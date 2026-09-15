package com.landerlab.lplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.landerlab.lplanner.BuildConfig
import com.landerlab.lplanner.PlannerModel
import com.landerlab.lplanner.ZPlan
import kotlin.math.roundToInt

/**
 * ConfigSheet.kt — Lplanner Android v1.0.0
 *
 * Every setting in one place, with the same descriptions as the SwiftUI ConfigSheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigSheet(m: PlannerModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        ) {
            Text("Config", style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold)
            Column(Modifier.weight(1f)) {}
            TextButton(onClick = onDismiss) { Text("Done") }
        }
        HorizontalDivider()

        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            ConfigGroup("Units") {
                SettingRow("Depths") {
                    Seg(listOf("Feet", "Meters"), if (m.depthsMetric) 1 else 0) { m.changeDepthUnits(it == 1) }
                }
                SettingRow("RMVs") {
                    Seg(listOf("Cu.ft.", "Liters"), if (m.rmvMetric) 1 else 0) { m.changeRmvUnits(it == 1) }
                }
            }

            ConfigGroup("Environment") {
                SettingRow("Water") {
                    Seg(listOf("Fresh", "Salt"), if (m.saltWater) 1 else 0) { m.saltWater = it == 1 }
                }
                SettingRow("O2 Narcotic") {
                    Seg(listOf("No", "Yes"), if (m.o2Narcotic) 1 else 0) { m.o2Narcotic = it == 1 }
                }
            }

            ConfigGroup("Model") {
                Seg(
                    listOf("ZHL16-C", "VVAL-79", "VPM-B"),
                    when (m.model) { "vval" -> 1; "vpm" -> 2; else -> 0 },
                    modifier = Modifier.fillMaxWidth(),
                ) { m.model = when (it) { 1 -> "vval"; 2 -> "vpm"; else -> "c" } }

                if (m.model == "c") {
                    Check("Gradient factors", m.useGF) { m.useGF = it }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        LabeledField("GF Low", m.gfLow, Modifier.weight(1f), enabled = m.useGF) { m.gfLow = it }
                        LabeledField("GF High", m.gfHigh, Modifier.weight(1f), enabled = m.useGF) { m.gfHigh = it }
                    }
                }
            }

            if (m.model == "vpm") {
                ConfigGroup("VPM-B") {
                    Column {
                        Text(
                            "Conservatism: ${m.vpmConservatism}  (0–4)",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        )
                        Slider(
                            value = m.vpmConservatism.toFloat(),
                            onValueChange = { m.vpmConservatism = it.roundToInt() },
                            valueRange = 0f..4f,
                            steps = 3,
                        )
                    }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        LabeledField("Radius N2 (µm)", m.vpmRadiusN2, Modifier.weight(1f)) { m.vpmRadiusN2 = it }
                        LabeledField("Radius He (µm)", m.vpmRadiusHe, Modifier.weight(1f)) { m.vpmRadiusHe = it }
                    }
                }
            }

            if (m.model == "c") {
                ConfigGroup("Alternative gradient factors") {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        LabeledField("Alt GF Low", m.altGfLow, Modifier.weight(1f)) { m.altGfLow = it }
                        LabeledField("Alt GF High", m.altGfHigh, Modifier.weight(1f)) { m.altGfHigh = it }
                    }
                }

                ConfigGroup("NDL calculation") {
                    Text("Calculate NDL by", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Seg(
                        listOf("GF High (standard)", "GF Low"),
                        if (m.ndlLow) 1 else 0,
                        enabled = m.gfOn,
                        modifier = Modifier.fillMaxWidth(),
                    ) { m.ndlLow = it == 1 }
                }
            }

            ConfigGroup("Conditions") {
                LabeledField("Altitude (${m.depthUnit})", m.altitude) { m.altitude = it }
                // Only above sea level, where the two references differ. At 0 m
                // equilibrated and just-arrived are the same tissue loading and
                // the control would be noise.
                if ((m.altitude.toDoubleOrNull() ?: 0.0) > 0.0) {
                    Check("Diver equilibrated at this altitude (about 12 h)", m.altitudeEquilibrated) {
                        m.altitudeEquilibrated = it
                    }
                    if (!m.altitudeEquilibrated) {
                        LabeledField("Hours at altitude", m.hoursAtAltitude, Modifier.width(190.dp)) {
                            m.hoursAtAltitude = it
                        }
                        Text(
                            "0 = arrived just now, carrying sea-level nitrogen.",
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
                        )
                    }
                }
                Column(Modifier.alphaIf(m.consOn)) {
                    Text(
                        when {
                            m.gfOn        -> "Conservatism — not used with gradient factors"
                            m.model != "c" -> "Conservatism — ZHL16-C only"
                            else           -> "Conservatism: ${m.conservatism.toInt()} %  (0–50 maximum)"
                        },
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    )
                    Slider(
                        value = m.conservatism.toFloat(),
                        onValueChange = { m.conservatism = it.roundToInt().toDouble() },
                        valueRange = 0f..50f,
                        steps = 49,
                        enabled = m.consOn,
                    )
                }
            }

            ConfigGroup("Stop depths") {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LabeledField("Stop distance (${m.depthUnit})", m.stopDistance, Modifier.weight(1f)) { m.stopDistance = it }
                    LabeledField("Last stop (${m.depthUnit})", m.lastStop, Modifier.weight(1f)) { m.lastStop = it }
                }
            }

            if (!(m.useGF && m.model == "c")) {
                ConfigGroup("Deep stops") {
                    Seg(
                        listOf("None", "Pyle"),
                        if (m.deepStops == "p") 1 else 0,
                        modifier = Modifier.fillMaxWidth(),
                    ) { m.deepStops = if (it == 1) "p" else "n" }

                    if (m.deepStops == "p") {
                        // "Time", not "Pyle stop time": the group is already
                        // called Deep stops and the only mode with a time is
                        // Pyle, so the prefix bought nothing and cost the width
                        // that pushed + off the right edge on a phone. The
                        // steppers are plain boxes rather than TextButtons,
                        // which carry ~16 dp of minimum padding each.
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                "Time: ${m.pyleTime} min  (1–5)",
                                maxLines = 1,
                                modifier = Modifier.weight(1f),
                            )
                            Stepper("−") { if (m.pyleTime > 1) m.pyleTime-- }
                            Stepper("+") { if (m.pyleTime < 5) m.pyleTime++ }
                        }
                    }
                }
            }

            ConfigGroup("Descent — range, rate (${m.depthUnit}/min)") {
                RateEditor(m.descentRates, minHeight = 60) { m.descentRates = it }
            }

            ConfigGroup("Ascent — range, rate (${m.depthUnit}/min, deepest first)") {
                RateEditor(m.ascentRates, minHeight = 96) { m.ascentRates = it }
            }

            ConfigGroup("Deco Set Point (CCR) / Slide rate") {
                RateEditor(m.decoSetpoints, enabled = m.circuitClosed, minHeight = 60) {
                    m.decoSetpoints = it
                }
                LabeledField("Slide rate  PO2/min", m.slideRate, Modifier.width(190.dp)) { m.slideRate = it }
            }

            ConfigGroup("Extended stops on a deco mix switch") {
                Stepper0to10(if (m.depthsMetric) "30 m+" else "100 ft+", m.extStopDeep) { m.extStopDeep = it }
                Stepper0to10(if (m.depthsMetric) "7–30 m" else "23–100 ft", m.extStopShallow) { m.extStopShallow = it }
            }

            ConfigGroup("Air breaks") {
                Check("Plan air breaks", m.airBreaksOn) { m.airBreaksOn = it }
                if (m.airBreaksOn) {
                    Seg(
                        listOf("Navy", "Subsurface"),
                        if (m.airBreakMode == "navy") 0 else 1,
                        modifier = Modifier.fillMaxWidth(),
                    ) { m.airBreakMode = if (it == 0) "navy" else "subsurface" }
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        LabeledField("Break after (min)", m.breakAfter, Modifier.weight(1f)) {
                            m.breakAfter = it
                        }
                        LabeledField("Break for (min)", m.breakFor, Modifier.weight(1f)) {
                            m.breakFor = it
                        }
                    }
                    LabeledField(
                        "Break gas (blank = automatic)", m.breakGas,
                        Modifier.width(240.dp), numeric = false,
                    ) { m.breakGas = it }
                }
            }

            ConfigGroup("Deco gas limits") {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LabeledField("Max PO2", m.maxPO2, Modifier.weight(1f)) { m.maxPO2 = it }
                    LabeledField("Max END (${m.depthUnit})", m.maxEND, Modifier.weight(1f)) { m.maxEND = it }
                }
            }

            ConfigGroup("RMV values") {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LabeledField("Bottom (${m.rmvUnit})", m.bottomRMV, Modifier.weight(1f)) { m.bottomRMV = it }
                    LabeledField("Deco (${m.rmvUnit})", m.decoRMV, Modifier.weight(1f)) { m.decoRMV = it }
                }
            }

            Text(
                "Lplanner ${BuildConfig.VERSION_NAME} · engine ZPlanKit ${ZPlan.version}",
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                color = androidx.compose.material3.MaterialTheme.colorScheme.outline,
            )
        }
    }
}

/** 0-10 minute picker, matching the Pyle stop-time control. */
@Composable
private fun Stepper0to10(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("$label : $value min", modifier = Modifier.width(140.dp))
        TextButton(onClick = { if (value > 0) onChange(value - 1) }) { Text("−") }
        TextButton(onClick = { if (value < 10) onChange(value + 1) }) { Text("+") }
    }
}

@Composable
private fun SettingRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(120.dp))
        Column(Modifier.width(240.dp)) { content() }
    }
}
