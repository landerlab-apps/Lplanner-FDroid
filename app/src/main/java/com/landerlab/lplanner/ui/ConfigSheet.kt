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
            ConfigGroup(
                "Units",
                "Depths sets the units for depth, altitude, stop distance and END. RMVs sets the " +
                    "units for breathing-rate and gas-consumption figures — the two can differ.",
            ) {
                SettingRow("Depths") {
                    Seg(listOf("Feet", "Meters"), if (m.depthsMetric) 1 else 0) { m.depthsMetric = it == 1 }
                }
                SettingRow("RMVs") {
                    Seg(listOf("Cu.ft.", "Liters"), if (m.rmvMetric) 1 else 0) { m.rmvMetric = it == 1 }
                }
            }

            ConfigGroup(
                "Environment",
                "Fresh or salt water changes the depth-to-pressure conversion. O2 Narcotic controls " +
                    "whether oxygen counts as narcotic when calculating equivalent narcotic depths (ENDs).",
            ) {
                SettingRow("Water") {
                    Seg(listOf("Fresh", "Salt"), if (m.saltWater) 1 else 0) { m.saltWater = it == 1 }
                }
                SettingRow("O2 Narcotic") {
                    Seg(listOf("No", "Yes"), if (m.o2Narcotic) 1 else 0) { m.o2Narcotic = it == 1 }
                }
            }

            ConfigGroup(
                "Model",
                "ZHL16-C is the Bühlmann set used here. VVAL-18 is the U.S. Navy Thalmann EL-DCM " +
                    "(exponential uptake, linear elimination); gradient factors and Conservatism do not " +
                    "apply to it. VPM-B (Yount/Hoffman/Baker) tracks bubble nuclei rather than dissolved " +
                    "gas tension — it tends to place the first stop deep, with shorter shallow stops. " +
                    "Gradient factors and Conservatism apply to ZHL16-C only.",
            ) {
                Seg(
                    listOf("ZHL16-C", "VVAL-18", "VPM-B"),
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
                ConfigGroup(
                    "VPM-B",
                    "Conservatism adds extra gas volume allowance on top of Baker's nominal schedule: " +
                        "0 is the published reference, 4 is the most conservative. Critical radii are the " +
                        "initial nucleus sizes in microns (N2 0.6, He 0.5 by default). Leave the radii " +
                        "alone — changing them moves you outside the validated envelope.",
                ) {
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
                ConfigGroup(
                    "Alternative gradient factors",
                    "A second GF pair, used instead of the main pair whenever altGF is checked on the " +
                        "main screen. Set these to whatever you like — any values are accepted, low and " +
                        "high independently, and they need not bracket the main pair. 100/100 gives the " +
                        "pure Buhlmann ZHL-16C ceiling; values above 100 go beyond it (less conservative " +
                        "than the raw model); a low GF Low with a high GF High deepens the first stop " +
                        "while keeping the shallow stops short. Editable here or directly beside the " +
                        "altGF checkbox on the main screen.",
                ) {
                    Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                        LabeledField("Alt GF Low", m.altGfLow, Modifier.weight(1f)) { m.altGfLow = it }
                        LabeledField("Alt GF High", m.altGfHigh, Modifier.weight(1f)) { m.altGfHigh = it }
                    }
                }

                ConfigGroup(
                    "NDL calculation",
                    "Which gradient factor decides whether a direct, no-stop ascent to the surface is " +
                        "still allowed. GF High is the standard behaviour for ZHL16-C. GF Low is stricter " +
                        "and ends the no-decompression phase earlier.",
                ) {
                    Text("Calculate NDL by", style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Seg(
                        listOf("GF High (standard)", "GF Low"),
                        if (m.ndlLow) 1 else 0,
                        enabled = m.gfOn,
                        modifier = Modifier.fillMaxWidth(),
                    ) { m.ndlLow = it == 1 }
                }
            }

            ConfigGroup(
                "Conditions",
                "Altitude of the dive site, 0 for sea level. Above sea level the air is thinner, so " +
                    "the same dive carries more decompression. Acclimatised means you have lived at " +
                    "this altitude long enough for your tissues to have equilibrated to it. If you " +
                    "drove up this morning you are still carrying your sea-level nitrogen and need " +
                    "considerably more decompression — at 3000 m that can double the obligation, so " +
                    "state it honestly. Hours at altitude covers the middle: the tissues wash out " +
                    "towards equilibrium at their own rates. Conservatism applies only when gradient " +
                    "factors are switched off. It (0–50 %) preloads the tissue compartments with " +
                    "additional inert gas — nitrogen, and helium in proportion when the profile uses " +
                    "trimix — weighted from the fast compartments (none) to the slow ones (the full " +
                    "percentage), as if a previous dive had been made. Zero is the clean-diver profile.",
            ) {
                LabeledField("Altitude", m.altitude) { m.altitude = it }
                // Only above sea level, where the two references differ. At 0 m
                // acclimatised and just-arrived are the same tissue loading and
                // the control would be noise.
                if ((m.altitude.toDoubleOrNull() ?: 0.0) > 0.0) {
                    Check("Diver acclimatised to this altitude", m.altitudeAcclimatised) {
                        m.altitudeAcclimatised = it
                    }
                    if (!m.altitudeAcclimatised) {
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

            // Stop grid stands on its own. It used to live inside Deep stops,
            // which hid it completely whenever gradient factors were on — yet
            // every schedule is built on this grid, GF or not, Pyle or not, and
            // a diver who wants 6 m increments on a rebreather has nothing to
            // do with deep stops.
            ConfigGroup(
                "Stop depths",
                "Stop distance is the interval between decompression stops — 3 m is the convention, " +
                    "some rebreather divers prefer 6 m. Last stop is the depth of the final stop; " +
                    "some prefer pulling the 10 ft / 3 m stop deeper. Both apply to every schedule, " +
                    "whichever model, gradient factors or deep stops are in use.",
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LabeledField("Stop distance", m.stopDistance, Modifier.weight(1f)) { m.stopDistance = it }
                    LabeledField("Last stop", m.lastStop, Modifier.weight(1f)) { m.lastStop = it }
                }
            }

            if (!(m.useGF && m.model == "c")) {
                ConfigGroup(
                    "Deep stops",
                    "Pyle deep stops insert short stops between the bottom and the first normal stop " +
                        "(mean-depth rule, re-run iteratively) to reduce microbubble formation and " +
                        "post-dive fatigue. Pyle stop time is the minutes spent at each generated stop " +
                        "(1–5). Not shown when gradient factors are enabled: GF Low takes over the " +
                        "deep-stop role.",
                ) {
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

            ConfigGroup(
                "Ascent behaviour (experimental)",
                "Extra slow delays the ascent to the next stop while the off-gassing gradient of any " +
                    "compartment — tissue inert tension minus ambient pressure, i.e. supersaturation — " +
                    "exceeds 1.25 bar. It only ever adds time at the deeper depth, so the schedule " +
                    "stays below the gradient factor regardless of the rule. Two limits keep it " +
                    "practical: it never applies to the final ascent to the surface, and it adds at " +
                    "most 5 minutes per stop. Time spent held is counted in the total decompression " +
                    "time. Noticeable on dives that leave a compartment strongly supersaturated at " +
                    "the stop.",
            ) {
                Check("Extra slow ascent rule", m.extraSlow) { m.extraSlow = it }
            }

            ConfigGroup(
                "Descent — range, rate",
                "One range per line: depth1-depth2, rate (ft or m per minute). List shallowest range " +
                    "first, leave no gaps.",
            ) {
                RateEditor(m.descentRates, minHeight = 60) { m.descentRates = it }
            }

            ConfigGroup(
                "Ascent — range, rate (deepest first)",
                "One range per line, deepest range first, no gaps. Slow shallow ascent rates are " +
                    "credited to the decompression and can shorten stops or remove them entirely.",
            ) {
                RateEditor(m.ascentRates, minHeight = 96) { m.ascentRates = it }
            }

            ConfigGroup(
                "Deco Set Point (CCR) / Slide rate",
                "Setpoint changes by depth range during CCR deco, one per line, e.g. 80-30, 1.4 — a " +
                    "setpoint of 0 switches to open circuit for that range. Only active when the circuit " +
                    "is set to CCR on the main screen (disabled for open-circuit dives). Slide rate is " +
                    "the PO2 burned off per minute during a Scamahorn Slide: enter a bottom setpoint like " +
                    "1.2-1.6 to ride the descent PO2 spike down to the setpoint for a deco advantage.",
            ) {
                RateEditor(m.decoSetpoints, enabled = m.circuitClosed, minHeight = 60) {
                    m.decoSetpoints = it
                }
                LabeledField("Slide rate  PO2/min", m.slideRate, Modifier.width(190.dp)) { m.slideRate = it }
            }

            ConfigGroup(
                "Extended stops on a deco mix switch",
                "Extra minutes held at the depth where the planner switches to a deco mix, " +
                    "on top of whatever the model requires. Common practice: settle on the new " +
                    "gas, confirm the analysis and the PO2, and let the switch do some work for " +
                    "you. The amount is chosen by the depth of the switch, in two bands. " +
                    "Switches shallower than 7 m / 23 ft are not extended — the final stop is " +
                    "already long. The extra time off-gasses you, so it does not simply add to " +
                    "the total: the stops above it usually shorten.",
            ) {
                Stepper0to10("30 m+", m.extStopDeep) { m.extStopDeep = it }
                Stepper0to10("7–30 m", m.extStopShallow) { m.extStopShallow = it }
            }

            ConfigGroup(
                "Deco gas limits",
                "The planner auto-selects the deco gas with the highest PO2 that stays within Max PO2 " +
                    "and Max END. Set Max PO2 to 1.6 if you want 100% O2 at the 20 ft / 6 m stop; tune it " +
                    "down to lower CNS exposure at the cost of longer deco.",
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LabeledField("Max PO2", m.maxPO2, Modifier.weight(1f)) { m.maxPO2 = it }
                    LabeledField("Max END", m.maxEND, Modifier.weight(1f)) { m.maxEND = it }
                }
            }

            ConfigGroup(
                "RMV values",
                "Respiratory Minute Volume for gas-consumption planning, in the RMV units above. Deco " +
                    "is usually lower than Bottom, since you are more at rest hanging on the line. If you " +
                    "don't know your RMV, measure it.",
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    LabeledField("Bottom", m.bottomRMV, Modifier.weight(1f)) { m.bottomRMV = it }
                    LabeledField("Deco", m.decoRMV, Modifier.weight(1f)) { m.decoRMV = it }
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
