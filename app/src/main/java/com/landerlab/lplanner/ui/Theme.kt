package com.landerlab.lplanner.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

/**
 * Theme.kt — Lplanner Android v1.0.0
 *
 * Strictly greyscale: black on white, greys for secondary text and strokes.
 * Nothing is coloured, including warnings — emphasis comes from weight and
 * placement instead.
 */

private val Mono = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    secondary = Color(0xFF444444),
    onSecondary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF333333),
    outline = Color(0xFF808080),
    outlineVariant = Color(0xFFCCCCCC),
    error = Color.Black,        // greyscale brand: no red anywhere
    onError = Color.White,
)

private val MonoDark = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = Color(0xFFBBBBBB),
    onSecondary = Color.Black,
    background = Color(0xFF101010),
    onBackground = Color.White,
    surface = Color(0xFF101010),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFDDDDDD),
    outline = Color(0xFF999999),
    outlineVariant = Color(0xFF444444),
    error = Color.White,        // greyscale brand: no red anywhere
    onError = Color.Black,
)

/** The plan report is fixed-width ASCII art — it must never be proportional. */
val MonoText = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
val MonoTextSmall = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 11.sp)

@Composable
fun LplannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) MonoDark else Mono,
        typography = Typography(),
        content = content,
    )
}
