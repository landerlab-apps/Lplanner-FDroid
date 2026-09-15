package com.landerlab.lplanner.ui

import android.content.Intent
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.landerlab.lplanner.BuildConfig
import com.landerlab.lplanner.ConfigGuide
import com.landerlab.lplanner.Disclaimer
import com.landerlab.lplanner.DiveLevel
import com.landerlab.lplanner.Manual
import com.landerlab.lplanner.PlannerModel
import com.landerlab.lplanner.Printing
import com.landerlab.lplanner.Support
import com.landerlab.lplanner.ZPlan

/**
 * PlannerScreen.kt — Lplanner Android v1.0.0
 *
 * Port of ZPlannerView (SwiftUI). Monochrome, no graphics.
 * Top bar: Config · Log · Calculate.
 * Phone: two tabs (Dive entry / Plan). Tablet: side-by-side, like the Mac layout.
 */
@Composable
fun PlannerScreen(m: PlannerModel) {
    var showConfig by remember { mutableStateOf(false) }
    var showLog by remember { mutableStateOf(false) }
    var showInfo by remember { mutableStateOf(false) }

    // The SwiftUI original switches on horizontalSizeClass == .compact.
    //
    // Height matters as much as width. A phone on its side reports ~870 x 390 dp
    // and so passed a width-only test, landing on the tablet layout — three
    // full-width setup rows plus the top bar left barely 100 dp for the plan,
    // which is why landscape was unusable. A real tablet is wide AND tall.
    // Measured from the WINDOW, not the screen. Configuration.screenWidthDp
    // reports the display; on a foldable, in split screen, or in a freeform
    // desktop window the app may own a fraction of it, and the layout would
    // switch on the size of a rectangle the app does not have. Compose flags
    // the old call for exactly this reason. containerSize is in pixels, so it
    // goes through the density to reach dp.
    val container = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val wide = with(density) {
        container.width.toDp() >= 600.dp && container.height.toDp() >= 480.dp
    }

    // Hoisted out of the narrow branch: pressing Calculate has to be able to
    // bring the Plan tab forward. On a phone the plan and every error message
    // render on the second tab, so calculating from the Dive tab produced no
    // visible change at all and looked like a dead button.
    var tab by remember { mutableIntStateOf(0) }

    // Keyed on `tab`, so switching tabs resets it: the chip strip folds away on
    // the Plan tab, where the schedule wants every line it can get, and returns
    // on the Dive tab. Keying rather than deriving still leaves the chevron free
    // to override it — the override just lasts until you change tabs again,
    // which is the point at which the default is right anyway.
    var chipsOpen by remember(tab) { mutableStateOf(tab == 0) }

    // Survives rotation, which matters: turning the phone sideways is the whole
    // reason to be in here, and dropping back to the tabbed view mid-turn would
    // undo the thing you just did.
    var planFull by rememberSaveable { mutableStateOf(false) }

    if (planFull) {
        PlanFullScreen(m) { planFull = false }
        return
    }

    Scaffold { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            // Log is now purely a viewer — entries are recorded by Calculate.
            TopBar(
                m,
                onConfig = { showConfig = true },
                onLog = { showLog = true },
                onInfo = { showInfo = true },
                onCalculate = {
                    m.calculate()
                    tab = 1          // show the result, or the reason there isn't one
                },
            )
            HorizontalDivider()

            // Tablet keeps the original three rows. The phone gets one wrapping
            // chip strip instead: those rows took roughly 40% of a 1080x2400
            // screen and the Actual field ran off the right edge.
            if (wide) {
                SurfaceIntervalRow(m)
                HorizontalDivider()
                DecoGasRow(m)
                HorizontalDivider()
                AutoRow(m)
                HorizontalDivider()
            } else {
                CompactSetupChips(m, expanded = chipsOpen) { chipsOpen = !chipsOpen }
                HorizontalDivider()
            }

            // Messages live above the tabs so they are visible whichever tab is
            // showing. Previously they rendered inside the Plan pane, so a
            // refusal like "no enabled dive levels" was invisible from the Dive
            // tab and Calculate looked like it had done nothing at all.
            if (m.notes.isNotEmpty()) {
                Text(
                    m.notes,
                    style = MonoTextSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                )
                HorizontalDivider()
            }

            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.width(300.dp).verticalScroll(rememberScrollState()).padding(12.dp)
                    ) { DiveColumn(m) }
                    androidx.compose.material3.VerticalDivider()
                    Column(
                        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)
                    ) { PlanPane(m) { planFull = true } }
                }
            } else {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Dive") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Plan") })
                }
                Column(
                    Modifier
                        .fillMaxSize()
                        // Without this the keyboard covers whatever is being
                        // typed. enableEdgeToEdge() means the manifest's
                        // adjustResize is not enough on its own — Compose has
                        // to be told about the IME inset.
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp)
                ) {
                    if (tab == 0) CompactDiveColumn(m) else PlanPane(m) { planFull = true }
                }
            }
        }
    }

    if (showConfig) ConfigSheet(m) { showConfig = false }
    if (showLog) LogSheet(m) { showLog = false }
    if (showInfo) InfoDialog { showInfo = false }
}

// ---- top bar: Config · Log · Calculate (left) · Share (right) ----

@Composable
private fun TopBar(
    m: PlannerModel,
    onConfig: () -> Unit,
    onLog: () -> Unit,
    onInfo: () -> Unit,
    onCalculate: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        BarButton("Config", Icons.Filled.Settings, onClick = onConfig)
        BarButton("Log", Icons.AutoMirrored.Filled.MenuBook, onClick = onLog)
        // Dimmed and inert until a surface interval is stated, when residual
        // gas is being carried — same 40% treatment as Share and Print.
        // "Calc" on the phone. The word is the widest thing in the bar and the
        // button is unmistakable at either length.
        Text(
            text = "Calc",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .alphaIf(m.canCalculate)
                .border(1.5.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
                .clickable(enabled = m.canCalculate, onClick = onCalculate)
                .padding(horizontal = 18.dp, vertical = 7.dp),
        )
        Box(Modifier.weight(1f))
        val noPlan = m.planText.isEmpty()
        BarButton("Share", Icons.Filled.Share, enabled = !noPlan, showLabel = false) {
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, m.planText)
            }
            context.startActivity(Intent.createChooser(send, "Share dive plan"))
        }
        BarButton("Print", Icons.Filled.Print, enabled = !noPlan, showLabel = false) {
            Printing.printPlan(context, m.planText)
        }
        BarButton("Info", Icons.Outlined.Info, showLabel = false, onClick = onInfo)
    }
}

@Composable
private fun InfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text("Lplanner", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Text(
                    Disclaimer.text,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(Manual.text, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    "CONFIG SETTINGS",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(ConfigGuide.text, style = MaterialTheme.typography.bodySmall)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                // F-Droid build only; see the note on Support. The file is
                // identical in both Android trees so drift-check stays clean.
                if (Support.showInBuild(BuildConfig.APPLICATION_ID)) {
                    Text(Support.heading, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium)
                    Text(Support.text, style = MaterialTheme.typography.bodySmall)
                    // Hands off to the browser via ACTION_VIEW, so the app still
                    // needs no INTERNET permission and makes no network access.
                    val uriHandler = LocalUriHandler.current
                    // Plain `if`, not `?.let {}`: the lambda `let` takes is not
                    // @Composable, so Text() cannot be called inside it.
                    val payUrl = Support.paypalUrl
                    if (payUrl != null) {
                        Text(
                            Support.linkLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable { uriHandler.openUri(payUrl) },
                        )
                    }
                    SelectionContainer {
                        Text(
                            Support.fallback,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                Text(
                    "Lplanner ${BuildConfig.VERSION_NAME} · engine ZPlanKit ${ZPlan.version} · AI-assisted",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        },
    )
}

/**
 * Bar button. [showLabel] false leaves the icon alone in the box — used for
 * Share, Print and Info, where the picture is unambiguous and the caption was
 * only costing width. The vertical padding grows to compensate so an icon-only
 * button stays the same height as a captioned one and the bar keeps one
 * baseline; without it Config and Log stood a row taller than the rest.
 */
@Composable
private fun BarButton(
    title: String,
    icon: ImageVector,
    enabled: Boolean = true,
    showLabel: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier
            .alphaIf(enabled)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = if (showLabel) 4.dp else 11.dp),
    ) {
        Icon(icon, contentDescription = title, modifier = Modifier.size(20.dp))
        if (showLabel) Text(title, style = MaterialTheme.typography.labelSmall)
    }
}

// ---- Surface Interval row ----

@Composable
private fun SurfaceIntervalRow(m: PlannerModel) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Surface Interval", style = MaterialTheme.typography.bodyMedium)
            Check("48 hr", m.si48) { on -> m.si48 = on; if (on) m.si24 = false }
            Check("24 hr", m.si24) { on -> m.si24 = on; if (on) m.si48 = false }
            Text("Actual:", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = m.siActual,
                onValueChange = { m.siActual = it.replace("\n", "") },
                placeholder = {
                    Text(if (m.hasResidual) m.elapsedText else "_:__", style = MonoText)
                },
                singleLine = true,
                textStyle = MonoText,
                modifier = Modifier.width(90.dp),
            )
        }
        // Residual loading must be visible. A schedule that silently depends on
        // an earlier dive is exactly what a diver has to be able to see and cancel.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (m.hasResidual) {
                Text(
                    if (m.canCalculate)
                        "Residual gas carried — surfaced ${m.elapsedText} ago"
                    else
                        "Residual gas carried — set a surface interval to calculate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
                Text(
                    "Clear",
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { m.clearTissues() },
                )
            } else {
                Text(
                    "No residual gas — planning clean",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            if (m.canCommit) {
                Text(
                    "Next dive",
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { m.commitDive() },
                )
            }
        }
    }
}

// ---- Deco gases row ----

@Composable
private fun DecoGasRow(m: PlannerModel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text("Deco gases", style = MaterialTheme.typography.bodyMedium)
        Check("Yes", m.decoGasesOn) { m.decoGasesOn = it }
        OutlinedTextField(
            value = m.decoGases,
            onValueChange = { m.decoGases = it.replace("\n", "") },
            placeholder = { Text("50, 100", style = MonoText) },
            singleLine = true,
            enabled = m.decoGasesOn,
            textStyle = MonoText,
            modifier = Modifier.width(160.dp).alphaIf(m.decoGasesOn),
        )
    }
}

// ---- +3m / +5min / altGF row ----

@Composable
private fun AutoRow(m: PlannerModel) {
    val gfUsable = m.model != "vval"
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Check(if (m.depthsMetric) "+3m" else "+10ft", m.plus3m) { m.plus3m = it }
        Check("+5min", m.plus5min) { m.plus5min = it }
        Check("travel gas", m.travelGas) { m.travelGas = it }
        Check("air breaks", m.airBreaksOn) { m.airBreaksOn = it }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.alphaIf(gfUsable),
        ) {
            Check("altGF", m.useAltGF, enabled = gfUsable) { m.useAltGF = it }
            // Editable here as well as in Config — any values accepted.
            OutlinedTextField(
                value = m.altGfLow,
                onValueChange = { m.altGfLow = it.replace("\n", "") },
                singleLine = true, enabled = gfUsable, textStyle = MonoText,
                modifier = Modifier.width(62.dp),
            )
            Text("/")
            OutlinedTextField(
                value = m.altGfHigh,
                onValueChange = { m.altGfHigh = it.replace("\n", "") },
                singleLine = true, enabled = gfUsable, textStyle = MonoText,
                modifier = Modifier.width(62.dp),
            )
        }
    }
}

// ---- Left column: circuit, entry fields, Add >>, levels list ----

@Composable
private fun DiveColumn(m: PlannerModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Seg(
            options = listOf("Open", "Closed"),
            selectedIndex = if (m.circuitClosed) 1 else 0,
            modifier = Modifier.fillMaxWidth(),
        ) { m.circuitClosed = it == 1 }

        Text("Depth, time, mix, levels.", style = MaterialTheme.typography.bodySmall)

        EntryField("D:", m.entry.d) { m.entry = m.entry.copy(d = it) }
        EntryField("T:", m.entry.t) { m.entry = m.entry.copy(t = it) }
        EntryField("O2:", m.entry.o2) { m.entry = m.entry.copy(o2 = it) }
        EntryField("He:", m.entry.he) { m.entry = m.entry.copy(he = it) }
        if (m.circuitClosed) {
            EntryField("Set:", m.entry.set) { m.entry = m.entry.copy(set = it) }
            EntryField("Sld:", m.entry.sld) { m.entry = m.entry.copy(sld = it) }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (m.editingID == null) "Add >>" else "Update",
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
                    .clickable { m.addEntry() }
                    .padding(horizontal = 14.dp, vertical = 5.dp),
            )
            if (m.editingID != null) {
                Text("Cancel", modifier = Modifier.clickable { m.cancelEdit() }.padding(5.dp))
            }
        }

        HorizontalDivider()
        Text(
            "Tap a level to edit it.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )

        m.levels.forEach { l -> LevelRow(m, l) }
    }
}

/** Phone version: the one-line entry row, then the levels right below it. */
@Composable
private fun CompactDiveColumn(m: PlannerModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        CompactEntryRow(m)
        HorizontalDivider()
        if (m.levels.isEmpty()) {
            Text(
                "No levels yet — type D, T and O2, then Add.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
        m.levels.forEach { l -> LevelRow(m, l) }
    }
}

@Composable
private fun LevelRow(m: PlannerModel, l: DiveLevel) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Check("", l.enabled) { m.setEnabled(l, it) }
        Text(
            text = l.summary,
            style = MonoText,
            textDecoration = if (m.editingID == l.id) TextDecoration.Underline else null,
            modifier = Modifier.weight(1f).clickable { m.beginEdit(l) },
        )
        IconAction(Icons.Filled.KeyboardArrowUp, "Move up") { m.move(l, up = true) }
        IconAction(Icons.Filled.KeyboardArrowDown, "Move down") { m.move(l, up = false) }
        IconAction(Icons.Filled.Close, "Remove level") { m.remove(l) }
    }
}

@Composable
private fun IconAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Icon(
        icon,
        contentDescription = description,
        modifier = Modifier.size(22.dp).clickable(onClick = onClick),
    )
}

@Composable
private fun EntryField(label: String, value: String, onChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(40.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { onChange(it.replace("\n", "")) },
            singleLine = true,
            textStyle = MonoText,
            modifier = Modifier.width(110.dp),
        )
    }
}

// ---- Plan output ----

@Composable
private fun PlanPane(m: PlannerModel, onFullScreen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (m.planText.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (m.canSaveLog) {
                    Row(
                        Modifier
                            .clickable { m.saveToLog() }
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.BookmarkBorder,
                            contentDescription = "Keep this plan in the log",
                            modifier = Modifier.size(20.dp),
                        )
                        Text("Keep", style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Row(
                        Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            "Kept",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                        )
                    }
                }
                Row(
                    Modifier.clickable(onClick = onFullScreen),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Full screen",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                    Icon(
                        Icons.Filled.Fullscreen,
                        contentDescription = "Read the plan full screen",
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
        // The report is fixed-width ASCII (~58 columns). Letting it soft-wrap
        // breaks every row — the EAD column folds onto the next line and stops
        // lining up with its header. Scroll horizontally instead of wrapping.
        SelectionContainer {
            Text(
                text = m.planText.ifEmpty { "Press Calculate." },
                style = MonoText,
                softWrap = false,
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            )
        }
    }
}
