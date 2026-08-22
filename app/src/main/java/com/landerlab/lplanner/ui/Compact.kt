package com.landerlab.lplanner.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.landerlab.lplanner.PlannerModel

/**
 * Compact.kt — phone layout for Lplanner Android.
 *
 * Only used when the screen is narrower than 600 dp. The tablet layout is left
 * exactly as it was, and so is macOS/iPad on the Apple side.
 *
 * The problem this solves: on a 1080x2400 phone the three setup rows plus the
 * stacked D/T/O2/He fields filled the screen, so the Add button and the level
 * list were never visible at once, the "Actual:" field ran off the right edge,
 * and the keyboard covered whatever was being typed.
 */

// ---------------------------------------------------------------- chips

/** Small outlined pill. Filled when active — no colour, per the brand. */
@Composable
fun Chip(label: String, active: Boolean = false, onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Text(
        label,
        style = MaterialTheme.typography.labelLarge,
        color = if (active) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current,
        modifier = Modifier
            .background(if (active) MaterialTheme.colorScheme.primary else Color.Transparent, shape)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    )
}

/**
 * One wrapping strip replacing the Surface Interval, Deco gases and +3m/+5min
 * rows. Toggles act immediately; anything needing a value opens a small dialog,
 * which is also what stops the layout overflowing at this width.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactSetupChips(m: PlannerModel, expanded: Boolean, onToggle: () -> Unit) {
    var editDeco by remember { mutableStateOf(false) }
    var editSI by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp)) {
        if (!expanded) {
            // One line: what is set, and a chevron to bring the chips back.
            // Reading a schedule needs height, not settings, so this folds away
            // on the Plan tab and returns on the Dive tab.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
            ) {
                Text(
                    settingsSummary(m),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.ExpandMore,
                    contentDescription = "Show settings",
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        if (expanded) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // OC / CCR rather than Open / Closed. As a segmented control both
                // states were on screen so "Open" was unambiguous; as a single chip
                // it has to name itself, and these are the terms divers use.
                Chip(if (m.circuitClosed) "CCR" else "OC", m.circuitClosed) {
                    m.circuitClosed = !m.circuitClosed
                }
                Chip(
                    if (m.decoGasesOn && m.decoGases.isNotBlank()) "Deco ${m.decoGases}" else "Deco off",
                    m.decoGasesOn,
                ) { editDeco = true }
                Chip(if (m.depthsMetric) "+3m" else "+10ft", m.plus3m) { m.plus3m = !m.plus3m }
                Chip("+5min", m.plus5min) { m.plus5min = !m.plus5min }
                // Toggle only. Carrying "90/90" in the label cost about a quarter
                // of the row to show two numbers that are set once and then left
                // alone; they are edited in Config.
                if (m.model != "vval") {
                    Chip("altGF", m.useAltGF) { m.useAltGF = !m.useAltGF }
                }
                Chip(
                    when {
                        m.siActual.isNotEmpty() -> "SI ${m.siActual}"
                        m.si48 -> "SI 48 hr"
                        m.si24 -> "SI 24 hr"
                        else -> "SI —"
                    },
                    m.repetitive,
                ) { editSI = true }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onToggle).padding(4.dp),
                ) {
                    Icon(
                        Icons.Filled.ExpandLess,
                        contentDescription = "Hide settings",
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Deliberately OUTSIDE the collapsible part. A carried-gas warning that
        // can be folded out of sight is the one thing here that must not be.
        // Only shown when gas is actually carried. "No residual gas — planning
        // clean" used to take three lines to say nothing had happened.
        if (m.hasResidual) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Text(
                    if (m.canCalculate) "Residual gas — surfaced ${m.elapsedText} ago"
                    else "Residual gas — set a surface interval",
                    style = MaterialTheme.typography.bodySmall,
                )
                Chip("Clear") { m.clearTissues() }
            }
        }
        if (m.canCommit) {
            Row(Modifier.padding(top = 6.dp)) {
                Chip("Next dive") { m.commitDive() }
            }
        }
    }

    if (editDeco) {
        EditDialog("Deco gases", onDismiss = { editDeco = false }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Chip(if (m.decoGasesOn) "On" else "Off", m.decoGasesOn) {
                    m.decoGasesOn = !m.decoGasesOn
                }
                NumField("mixes", m.decoGases, Modifier.width(140.dp), digitsOnly = false) {
                    m.decoGases = it
                }
            }
            Text(
                "Comma separated, e.g. 50, 100.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }

    if (editSI) {
        EditDialog("Surface interval", onDismiss = { editSI = false }) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("48 hr", m.si48) { m.si48 = !m.si48; if (m.si48) m.si24 = false }
                Chip("24 hr", m.si24) { m.si24 = !m.si24; if (m.si24) m.si48 = false }
            }
            NumField(
                "actual  h:mm", m.siActual, Modifier.width(140.dp), digitsOnly = false,
            ) { m.siActual = it }
            if (m.hasResidual) {
                Text(
                    "Surfaced ${m.elapsedText} ago. Use that or less.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

/** Terse one-line rendering of the chip strip, for when it is folded away. */
private fun settingsSummary(m: PlannerModel): String = buildList {
    add(if (m.circuitClosed) "CCR" else "OC")
    if (m.decoGasesOn && m.decoGases.isNotBlank()) add("Deco ${m.decoGases}")
    if (m.plus3m) add(if (m.depthsMetric) "+3m" else "+10ft")
    if (m.plus5min) add("+5min")
    // The numbers are worth showing here even though the chip no longer
    // carries them — this is text, and it costs nothing.
    if (m.useAltGF && m.model != "vval") add("altGF ${m.altGfLow}/${m.altGfHigh}")
    when {
        m.siActual.isNotEmpty() -> add("SI ${m.siActual}")
        m.si48 -> add("SI 48 hr")
        m.si24 -> add("SI 24 hr")
    }
}.joinToString("  ·  ")

@Composable
private fun EditDialog(title: String, onDismiss: () -> Unit, body: @Composable () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done") } },
        title = { Text(title, style = MaterialTheme.typography.titleSmall) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) { body() } },
    )
}

// ---------------------------------------------------------------- fields

/**
 * Compact numeric field: a label above a bordered box about 40 dp tall, versus
 * the ~56 dp OutlinedTextField it replaces. Opens the number pad rather than a
 * full QWERTY keyboard, which is both smaller and harder to mistype into.
 */
@Composable
fun NumField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    digitsOnly: Boolean = true,
    onChange: (String) -> Unit,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
        )
        BasicTextField(
            value = value,
            onValueChange = { raw ->
                val cleaned = raw.replace("\n", "")
                onChange(if (digitsOnly) cleaned.filter { it.isDigit() || it == '.' } else cleaned)
            },
            singleLine = true,
            textStyle = MonoText.copy(
                color = LocalContentColor.current,
                textAlign = TextAlign.Center,
            ),
            cursorBrush = SolidColor(LocalContentColor.current),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (digitsOnly) KeyboardType.Decimal else KeyboardType.Text,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(horizontal = 4.dp, vertical = 9.dp),
        )
    }
}

/**
 * The whole dive entry on one line: D, T, O2, He and Add. Four stacked rows
 * plus a button previously pushed the level list off the screen entirely.
 * Set and Sld appear on a second line only on closed circuit.
 */
@Composable
fun CompactEntryRow(m: PlannerModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            NumField("D", m.entry.d, Modifier.weight(1f)) { m.entry = m.entry.copy(d = it) }
            NumField("T", m.entry.t, Modifier.weight(1f)) { m.entry = m.entry.copy(t = it) }
            NumField("O2", m.entry.o2, Modifier.weight(1f)) { m.entry = m.entry.copy(o2 = it) }
            NumField("He", m.entry.he, Modifier.weight(1f)) { m.entry = m.entry.copy(he = it) }
            Column {
                Text(" ", style = MaterialTheme.typography.labelSmall)
                Chip(if (m.editingID == null) "Add" else "Save") { m.addEntry() }
            }
        }
        if (m.circuitClosed) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                NumField("Set", m.entry.set, Modifier.width(90.dp)) { m.entry = m.entry.copy(set = it) }
                NumField("Sld", m.entry.sld, Modifier.width(90.dp)) { m.entry = m.entry.copy(sld = it) }
                if (m.editingID != null) {
                    Column {
                        Text(" ", style = MaterialTheme.typography.labelSmall)
                        Chip("Cancel") { m.cancelEdit() }
                    }
                }
            }
        } else if (m.editingID != null) {
            Chip("Cancel edit") { m.cancelEdit() }
        }
    }
}
