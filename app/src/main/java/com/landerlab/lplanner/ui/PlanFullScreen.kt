package com.landerlab.lplanner.ui

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.landerlab.lplanner.PlannerModel
import kotlin.math.roundToInt

/**
 * PlanFullScreen.kt — the schedule and nothing else.
 *
 * Written for reading a plan off the screen while copying it onto a slate, in
 * sunlight, with wet hands. That drives every decision here:
 *
 *  - The report is fixed-width ASCII about 58 columns across, so the type size
 *    is COMPUTED to make those columns exactly fill the screen rather than
 *    guessed. Turning the phone sideways therefore makes the text genuinely
 *    bigger, not merely wider — which is the one thing the tabbed view could
 *    never do, and why landscape felt useless before.
 *  - Nothing ever wraps. A folded line puts EAD under the wrong header, and a
 *    misread deco schedule is not a cosmetic problem.
 *  - The screen is held awake. A plan that blanks halfway through transcribing
 *    it is worse than no plan.
 *  - Brightness goes to full on request, because none of the above matters if
 *    the screen is washed out in the sun.
 *  - Sizing is by button, not pinch. Pinch competes with the scrollers for the
 *    same drag events, and wet or gloved hands are bad at it.
 */
@Composable
fun PlanFullScreen(m: PlannerModel, onClose: () -> Unit) {

    /** 1.0 means "exactly fits the width". Survives rotation. */
    var zoom by rememberSaveable { mutableFloatStateOf(1f) }
    var chrome by rememberSaveable { mutableStateOf(true) }
    var sunlight by rememberSaveable { mutableStateOf(false) }

    BackHandler(onBack = onClose)

    // Hold the screen awake for as long as this view is up, and only that long.
    val view = LocalView.current
    DisposableEffect(Unit) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }

    // Full brightness overrides the system setting for this window only, and is
    // handed back on the way out. Leaving a phone pinned at maximum brightness
    // would quietly drain the battery of someone who is about to go diving.
    val activity = LocalContext.current as? Activity
    DisposableEffect(sunlight) {
        fun setBrightness(value: Float) {
            val window = activity?.window ?: return
            window.attributes = window.attributes.apply { screenBrightness = value }
        }
        setBrightness(
            if (sunlight) 1f else WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        )
        onDispose { setBrightness(WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE) }
    }

    val text = m.planText.ifEmpty { "No plan yet — press Calculate." }
    val taps = remember { MutableInteractionSource() }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val measurer = rememberTextMeasurer()

            // Converge on the size by measuring at the size, rather than
            // measuring once at a reference size and scaling.
            //
            // Two earlier attempts predicted the width and both came out ~20%
            // too large, which put the dashed rules and the EAD column off the
            // right edge at a nominal 100% "fit". The prediction is what was
            // wrong, not the arithmetic: sp is not a linear unit. Android 14
            // scales text non-linearly when the reader has changed the system
            // font size, compressing large sizes more than small ones, so a
            // width measured at 50 sp simply does not divide down to a width at
            // 11 sp. Font fallback for the ↓ ↑ — glyphs, which the monospace
            // face may not carry, can skew it too.
            //
            // Measuring at the candidate size assumes none of that. Four passes
            // settle it, and the loop below then guarantees the result actually
            // fits rather than merely aiming to.
            val fitted = remember(text, maxWidth) {
                val style = TextStyle(fontFamily = FontFamily.Monospace)
                val availablePx = with(density) { (maxWidth - 18.dp).toPx() }
                fun widthAt(size: Float) = measurer.measure(
                    AnnotatedString(text),
                    style.copy(fontSize = size.sp),
                    softWrap = false,
                ).size.width.toFloat()

                var size = 14f
                repeat(4) {
                    val w = widthAt(size)
                    if (w > 0f) size = (size * availablePx / w).coerceIn(7f, 48f)
                }
                // Belt and braces: shrink until it genuinely fits. A schedule
                // that is 2% too wide loses its last column, which is the one
                // failure this whole screen exists to prevent.
                var guard = 0
                while (guard++ < 10 && size > 7f && widthAt(size) > availablePx) {
                    size = (size * 0.98f).coerceAtLeast(7f)
                }
                size
            }

            val shown = (fitted * zoom).coerceIn(7f, 48f)

            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = shown.sp,
                    lineHeight = (shown * 1.35f).sp,
                ),
                softWrap = false,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                    // Inside the scrollers, so a drag scrolls and only a clean
                    // tap toggles the controls. No ripple: this is a whole
                    // screen of text, not a button.
                    .clickable(interactionSource = taps, indication = null) {
                        chrome = !chrome
                    }
                    .padding(
                        start = 8.dp,
                        end = 8.dp,
                        top = 4.dp,
                        // Clear the control bar rather than letting it sit on
                        // top of the schedule. Invisible in portrait, where
                        // there is height to spare; in landscape it was hiding
                        // a stop.
                        bottom = if (chrome) 60.dp else 4.dp,
                    ),
            )
        }

        if (chrome) {
            Column(Modifier.align(Alignment.BottomCenter)) {
                HorizontalDivider()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Chip("A−") { zoom = (zoom * 0.85f).coerceAtLeast(0.4f) }
                    Chip("A+") { zoom = (zoom * 1.18f).coerceAtMost(4f) }
                    Chip("Fit", zoom in 0.99f..1.01f) { zoom = 1f }
                    Chip("Sun", sunlight) { sunlight = !sunlight }
                    // Just the number. "· tap to hide" folded onto a second
                    // line in portrait once four chips were in front of it, and
                    // a hint that makes the bar taller is not worth its keep —
                    // it is in the manual instead.
                    Text(
                        "${(zoom * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        Modifier.size(36.dp).clickable(onClick = onClose),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close full screen",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }
        }
    }
}
