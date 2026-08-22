package com.landerlab.lplanner

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt

/**
 * PlannerModel.kt — Lplanner Android v1.0.0
 *
 * Direct port of PlannerModel in ZPlannerUI/ZPlannerView.swift. The profile-text
 * builder below is kept line-for-line identical to the Swift original: it is the
 * only thing the engine sees, so any divergence here would silently change dive
 * plans between the two platforms.
 */

data class DiveLevel(
    val id: String = UUID.randomUUID().toString(),
    var enabled: Boolean = true,
    var d: String = "",
    var t: String = "",
    var o2: String = "",
    var he: String = "",
    var set: String = "",
    var sld: String = "",
) {
    val summary: String
        get() = buildString {
            append("$d, $t, $o2")
            he.toDoubleOrNull()?.let { if (it > 0) append("/$he") }
            if (set.isNotEmpty()) append(", $set")
            if (sld.isNotEmpty()) append("-$sld")
        }
}

data class LogEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Date = Date(),
    /** Which dive and settings produced this plan, so entries are identifiable. */
    val summary: String,
    val text: String,
) {
    /** Date as well as time — entries from different days were indistinguishable. */
    val stamp: String
        get() = SimpleDateFormat("d MMM  HH:mm", Locale.getDefault()).format(date)
}

class PlannerModel(app: Application) : AndroidViewModel(app) {

    // Log entries and the entered dive state are persisted to the app's private
    // storage; without this both were discarded when the app closed.
    private val store = LogStore(app)
    private val stateStore = StateStore(app)


    private fun apply(s: PlannerState) {
        depthsMetric = s.depthsMetric; rmvMetric = s.rmvMetric
        saltWater = s.saltWater; o2Narcotic = s.o2Narcotic
        model = s.model
        vpmConservatism = s.vpmConservatism
        vpmRadiusN2 = s.vpmRadiusN2; vpmRadiusHe = s.vpmRadiusHe
        useGF = s.useGF; gfLow = s.gfLow; gfHigh = s.gfHigh
        altGfLow = s.altGfLow; altGfHigh = s.altGfHigh
        extraSlow = s.extraSlow; ndlLow = s.ndlLow
        altitude = s.altitude; conservatism = s.conservatism
        deepStops = s.deepStops; pyleTime = s.pyleTime
        stopDistance = s.stopDistance; lastStop = s.lastStop
        descentRates = s.descentRates; ascentRates = s.ascentRates
        decoSetpoints = s.decoSetpoints; slideRate = s.slideRate
        maxPO2 = s.maxPO2; maxEND = s.maxEND
        bottomRMV = s.bottomRMV; decoRMV = s.decoRMV
        extStopShallow = s.extStopShallow; extStopDeep = s.extStopDeep
        si48 = s.si48; si24 = s.si24; siActual = s.siActual
        decoGasesOn = s.decoGasesOn; decoGases = s.decoGases
        circuitClosed = s.circuitClosed
        plus3m = s.plus3m; plus5min = s.plus5min; useAltGF = s.useAltGF
        levels.clear(); levels.addAll(s.levels)
        baselineTissue = s.baselineTissue; baselineDate = s.baselineDate
    }

    private fun snapshot(): PlannerState {
        val s = PlannerState()
        s.depthsMetric = depthsMetric; s.rmvMetric = rmvMetric
        s.saltWater = saltWater; s.o2Narcotic = o2Narcotic
        s.model = model
        s.vpmConservatism = vpmConservatism
        s.vpmRadiusN2 = vpmRadiusN2; s.vpmRadiusHe = vpmRadiusHe
        s.useGF = useGF; s.gfLow = gfLow; s.gfHigh = gfHigh
        s.altGfLow = altGfLow; s.altGfHigh = altGfHigh
        s.extraSlow = extraSlow; s.ndlLow = ndlLow
        s.altitude = altitude; s.conservatism = conservatism
        s.deepStops = deepStops; s.pyleTime = pyleTime
        s.stopDistance = stopDistance; s.lastStop = lastStop
        s.descentRates = descentRates; s.ascentRates = ascentRates
        s.decoSetpoints = decoSetpoints; s.slideRate = slideRate
        s.maxPO2 = maxPO2; s.maxEND = maxEND
        s.bottomRMV = bottomRMV; s.decoRMV = decoRMV
        s.extStopShallow = extStopShallow; s.extStopDeep = extStopDeep
        s.si48 = si48; s.si24 = si24; s.siActual = siActual
        s.decoGasesOn = decoGasesOn; s.decoGases = decoGases
        s.circuitClosed = circuitClosed
        s.plus3m = plus3m; s.plus5min = plus5min; s.useAltGF = useAltGF
        s.levels = levels.toList()
        s.baselineTissue = baselineTissue; s.baselineDate = baselineDate
        return s
    }

    /**
     * Write the entered dive state to disk. Called when the app leaves the
     * foreground, and after any change to the levels list. Compose state has no
     * single change stream to hook, so these are the deliberate save points.
     */
    fun saveState() { stateStore.save(snapshot()) }

    // ---- Config sheet ----
    var depthsMetric by mutableStateOf(true)        // Depths: Feet / Meters
    var rmvMetric by mutableStateOf(true)           // RMVs: Cu.ft / Liters
    var saltWater by mutableStateOf(true)           // Water: Fresh / Salt
    var o2Narcotic by mutableStateOf(false)         // O2 Narcotic: No / Yes
    var model by mutableStateOf("c")                // "c" (ZHL16-C), "vval", or "vpm"
    var vpmConservatism by mutableStateOf(0)        // 0-4
    var vpmRadiusN2 by mutableStateOf("0.6")        // initial critical radius N2, microns
    var vpmRadiusHe by mutableStateOf("0.5")        // initial critical radius He, microns
    var useGF by mutableStateOf(false)
    var gfLow by mutableStateOf("30")
    var gfHigh by mutableStateOf("85")
    var altGfLow by mutableStateOf("90")
    var altGfHigh by mutableStateOf("90")
    var extraSlow by mutableStateOf(false)
    var ndlLow by mutableStateOf(false)
    var altitude by mutableStateOf("0")
    var conservatism by mutableStateOf(10.0)        // 0-100 %
    var deepStops by mutableStateOf("p")            // n / p
    var pyleTime by mutableStateOf(1)               // 1-5 min
    var stopDistance by mutableStateOf("3")
    var lastStop by mutableStateOf("3")
    var descentRates by mutableStateOf("0-100, 15")
    var ascentRates by mutableStateOf("70-30, 18\n30-12, 9\n12-0, 3")
    var decoSetpoints by mutableStateOf("")         // e.g. "80-30, 1.4\n29-0, 1.2"
    var slideRate by mutableStateOf("0.1")
    var maxPO2 by mutableStateOf("1.6")
    var maxEND by mutableStateOf("40")
    var bottomRMV by mutableStateOf("19")
    var decoRMV by mutableStateOf("14")
    /** Extra hold on a deco mix switch, per depth band, 0-10 min. */
    var extStopShallow by mutableStateOf(0)
    var extStopDeep by mutableStateOf(0)

    // ---- Main window rows ----
    var si48 by mutableStateOf(false)
    var si24 by mutableStateOf(false)
    var siActual by mutableStateOf("")              // H:MM
    var decoGasesOn by mutableStateOf(true)
    var decoGases by mutableStateOf("50")
    var circuitClosed by mutableStateOf(false)      // Open / Closed
    var plus3m by mutableStateOf(false)             // add 3 m / 10 ft to deepest level
    var plus5min by mutableStateOf(false)           // add 5 min to deepest level
    var useAltGF by mutableStateOf(false)           // use Alternative GF pair

    // ---- Levels ----
    val levels = mutableStateListOf<DiveLevel>()
    var entry by mutableStateOf(DiveLevel())
    var editingID by mutableStateOf<String?>(null)

    // ---- Output ----
    var planText by mutableStateOf("")
    var notes by mutableStateOf("")
    val log = mutableStateListOf<LogEntry>().apply { addAll(store.load()) }
    /** Loading at the start of the dive being planned; survives quitting. */
    var baselineTissue by mutableStateOf<String?>(null)
    var baselineDate by mutableStateOf(0L)
    /**
     * Loading at the END of the most recent calculation, not yet committed.
     * Observable, because the "Next dive" button's visibility depends on it and
     * a plain field would not recompose when Calculate or commitDive changed it.
     */
    private var resultTissue by mutableStateOf<String?>(null)

    init {
        // Must run after every property above is initialised: Kotlin executes
        // initialisers in declaration order, so restoring state before the
        // mutableStateOf delegates exist would dereference null.
        stateStore.load()?.let { apply(it) }
    }

    /** Gradient factors active — they override Conservatism. ZHL-16C only. */
    val gfOn: Boolean get() = (useGF || useAltGF) && model == "c"

    /** True when the Conservatism % slider is live (ZHL-16C, no GF). */
    val consOn: Boolean get() = model == "c" && !gfOn

    /** True when residual loading from an earlier dive is being carried. */
    val hasResidual: Boolean get() = baselineTissue != null

    /** Real time since the residual was recorded, in minutes. */
    val elapsedMinutes: Double
        get() = if (baselineDate == 0L) 0.0
                else ((System.currentTimeMillis() - baselineDate).coerceAtLeast(0L)) / 60000.0

    val elapsedText: String
        get() {
            val m = Math.round(elapsedMinutes).toInt()
            return String.format(Locale.US, "%d:%02d", m / 60, m % 60)
        }

    val repetitive: Boolean get() = si48 || si24 || siActual.isNotEmpty()

    /**
     * While residual gas is carried, a surface interval must be stated before a
     * plan can be produced. Guessing it from the clock would let a diver get a
     * schedule without ever confronting the fact that a previous dive is still
     * loaded, which is the one thing a repetitive plan must not hide.
     */
    val canCalculate: Boolean get() = !hasResidual || repetitive

    /**
     * A typed surface interval wins, so what-if planning still works. Otherwise
     * the real elapsed time since the residual was recorded is used, which is
     * what makes tracking advance while the app is closed.
     */
    val surfaceInterval: String
        get() = when {
            siActual.isNotEmpty() -> siActual
            si48 -> "48:00"
            si24 -> "24:00"
            hasResidual -> elapsedText
            else -> "900:00"
        }

    val profileText: String
        get() {
            val p = StringBuilder()
            // Built line by line rather than with a trimIndent() raw string: trimIndent
            // measures indentation AFTER interpolation, so a newline pasted into any
            // config field would silently mangle every following key.
            listOf(
                "UseMetric: ${yn(depthsMetric)}",
                "RmvMetric: ${yn(rmvMetric)}",
                "SaltWater: ${yn(saltWater)}",
                "Model: ${when (model) { "vval" -> "vval18"; "vpm" -> "vpm"; else -> "zhl16c" }}",
                "Altitude: ${one(altitude)}",
                "Conservatism: ${conservatism.toInt()}",
                "Precision: 1",
                "StopDistance: ${one(stopDistance)}",
                "LastStopDepth: ${one(lastStop)}",
                "OxyNarc: ${yn(o2Narcotic)}",
                "DeepStops: $deepStops",
                "PyleStopTime: $pyleTime",
                "TissueFile:",
                "SurfaceInterval: ${one(surfaceInterval)}",
                "Rmv: ${one(bottomRMV)}",
                "DecoRmv: ${one(decoRMV)}",
                "SlideRate: ${one(slideRate)}",
                "UseOCDeco: ${yn(decoGasesOn)}",
                "OcDecoGas: ${one(decoGases)}",
                "OcDecoMaxPO2: ${one(maxPO2)}",
                "MaxEND: ${one(maxEND)}",
                "ExtStopShallow: $extStopShallow",
                "ExtStopDeep: $extStopDeep",
            ).joinTo(p, "\n")
            if (model == "vpm") {
                p.append("\nVpmConservatism: $vpmConservatism")
                p.append("\nVpmRadiusN2: ${one(vpmRadiusN2)}")
                p.append("\nVpmRadiusHe: ${one(vpmRadiusHe)}")
            }
            if (model == "c" && (useGF || useAltGF)) {
                val lo = if (useAltGF) altGfLow else gfLow
                val hi = if (useAltGF) altGfHigh else gfHigh
                p.append("\nGradientFactors: $lo, $hi")
            }
            p.append("\nExtraSlow: ${if (extraSlow) "y" else "n"}")
            p.append("\nNdlGF: ${if (ndlLow) "low" else "high"}")
            for (r in descentRates.lines().filter { it.isNotBlank() }) p.append("\nDescentRate: $r")
            for (r in ascentRates.lines().filter { it.isNotBlank() }) p.append("\nAscentRate: $r")
            if (decoSetpoints.isBlank()) {
                p.append("\nUseDecoSetpoint: n")
            } else {
                p.append("\nUseDecoSetpoint: y")
                for (r in decoSetpoints.lines().filter { it.isNotBlank() }) p.append("\nDecoSetpoint: $r")
            }
            p.append("\n")

            // +3m / +5min apply to the deepest enabled level
            val enabled = levels.filter { it.enabled }
            val deepest = enabled.mapNotNull { it.d.toDoubleOrNull() }.maxOrNull() ?: 0.0
            for (l in enabled) {
                var d = l.d.toDoubleOrNull() ?: 0.0
                var t = l.t.toDoubleOrNull() ?: 0.0
                if (d >= deepest - 0.001 && deepest > 0) {
                    if (plus3m) d += if (depthsMetric) 3.0 else 10.0
                    if (plus5min) t += 5.0
                }
                val line = StringBuilder("${fmt(d)}, ${fmt(t)}, ${l.o2}")
                l.he.toDoubleOrNull()?.let { if (it > 0) line.append("/${l.he}") }
                if (l.set.isNotEmpty()) line.append(", ${l.set}")
                if (l.sld.isNotEmpty()) line.append("-${l.sld}")
                p.append(line).append("\n")
            }
            return p.toString()
        }

    private fun yn(b: Boolean) = if (b) "y" else "n"

    /** Collapse any stray newlines so one field can never spill into the next key. */
    private fun one(s: String) = s.replace('\n', ' ').replace('\r', ' ').trim()

    private fun fmt(v: Double): String =
        if (v == v.roundToInt().toDouble()) v.roundToInt().toString() else v.toString()

    /** Add a new level, or commit changes to the one being edited. */
    fun addEntry() {
        if (entry.d.isEmpty() || entry.t.isEmpty() || entry.o2.isEmpty()) return
        val id = editingID
        if (id != null) {
            val i = levels.indexOfFirst { it.id == id }
            if (i >= 0) {
                val wasEnabled = levels[i].enabled
                levels[i] = entry.copy(id = id, enabled = wasEnabled)
            }
            editingID = null
        } else {
            levels.add(entry)
        }
        entry = DiveLevel()
        saveState()
    }

    /** Load an existing level back into the entry fields for editing. */
    fun beginEdit(l: DiveLevel) {
        entry = l.copy()
        editingID = l.id
        if (l.set.isNotEmpty() || l.sld.isNotEmpty()) circuitClosed = true
    }

    fun cancelEdit() {
        editingID = null
        entry = DiveLevel()
    }

    fun move(l: DiveLevel, up: Boolean) {
        val i = levels.indexOfFirst { it.id == l.id }
        if (i < 0) return
        val j = if (up) i - 1 else i + 1
        if (j !in levels.indices) return
        val tmp = levels[i]; levels[i] = levels[j]; levels[j] = tmp
        saveState()
    }

    fun remove(l: DiveLevel) {
        if (editingID == l.id) cancelEdit()
        levels.removeAll { it.id == l.id }
        saveState()
    }

    fun setEnabled(l: DiveLevel, on: Boolean) {
        val i = levels.indexOfFirst { it.id == l.id }
        if (i >= 0) levels[i] = levels[i].copy(enabled = on)
        saveState()
    }

    fun calculate() {
        Log.i(TAG, "calculate: levels=${levels.size} enabled=${levels.count { it.enabled }} " +
                   "residual=$hasResidual canCalculate=$canCalculate")
        if (!canCalculate) {
            notes = "Residual gas is carried from an earlier dive. " +
                "Set the surface interval — 48 hr, 24 hr, or Actual — before calculating."
            planText = ""
            return
        }
        if (levels.none { it.enabled }) {
            notes = "No enabled dive levels — add a Depth / Time / O2 row first."
            planText = ""
            return
        }
        try {
            // Always planned from the baseline, never from the previous
            // result, so recalculating an edited dive never stacks it onto
            // itself.
            val r = ZPlan.plan(profileText, baselineTissue)
            Log.i(TAG, "calculate: engine returned ${r.reportText.length} chars")
            // Android only: the engine appends its warnings to the end of the
            // report. On a phone that block can run to six or seven wrapped
            // lines under a schedule that is already fighting for height, so it
            // is stripped here and the standing warning lives in Info instead.
            // zp_report() appends exactly "\n" + warnings, so removing that
            // suffix is exact rather than a guess at where the table ends.
            // iOS and macOS keep the warnings in the report.
            planText = if (r.warnings.isNotEmpty())
                r.reportText.removeSuffix("\n" + r.warnings).trimEnd() + "\n"
            else r.reportText
            // NOT r.warnings. The notes line is for the reasons there is NO plan.
            notes = ""
            resultTissue = r.tissueFileText
            // Log at the moment of calculation. Logging used to happen when the
            // Log button was pressed, which saved whatever planText happened to
            // hold — i.e. the previous calculation if any setting had changed
            // since — and appended a duplicate every time the log was merely
            // viewed. Recording it here means an entry always matches the
            // settings that produced it.
            // Prepared here, saved only if the diver asks for it. Building the
            // entry at this moment is what keeps it honest: it captures the
            // plan and the settings that produced it together. Logging on a
            // later button press was the old bug — it saved whatever planText
            // happened to hold by then, which was the PREVIOUS calculation if
            // anything had been changed since.
            pendingLog = LogEntry(summary = diveSummary, text = planText)
            saveState()
        } catch (e: ZPlanException) {
            Log.w(TAG, "calculate: engine refused the profile", e)
            planText = ""
            notes = e.message ?: "Decompression planning failed"
        } catch (e: Throwable) {
            // A native failure would otherwise take the whole app down with no
            // clue on screen. Report it where the diver can see it.
            Log.e(TAG, "calculate: unexpected failure", e)
            planText = ""
            notes = "Planner error: ${e.javaClass.simpleName} ${e.message ?: ""}"
        }
    }

    private companion object { const val TAG = "Lplanner" }

    /** Short description of the dive and the settings behind a logged plan. */
    private val diveSummary: String
        get() {
            val du = if (depthsMetric) "m" else "ft"
            val dive = levels.filter { it.enabled }.joinToString(" + ") { l ->
                buildString {
                    append("${l.d}$du/${l.t}min ${l.o2}%")
                    l.he.toDoubleOrNull()?.let { if (it > 0) append("/${l.he}he") }
                }
            }.ifEmpty { "no levels" }

            // Named modelText, not model: a local called `model` would shadow the
            // property of the same name and read very confusingly.
            val modelText = when (model) {
                "vval" -> "VVAL-18"
                "vpm"  -> "VPM-B +$vpmConservatism"
                else   -> buildString {
                    append("ZHL16-C")
                    if (gfOn) {
                        val lo = if (useAltGF) altGfLow else gfLow
                        val hi = if (useAltGF) altGfHigh else gfHigh
                        append(" GF$lo/$hi")
                    } else append(" cons ${conservatism.toInt()}%")
                }
            }

            val extras = buildList {
                if (circuitClosed) add("CCR")
                if (decoGasesOn && decoGases.isNotBlank()) add("deco $decoGases")
                if (deepStops == "p" && !gfOn) add("Pyle $pyleTime min")
                if (extraSlow) add("extra-slow")
                if (extStopShallow > 0 || extStopDeep > 0)
                    add("ext stops $extStopDeep/$extStopShallow min")
                if (plus3m) add(if (depthsMetric) "+3m" else "+10ft")
                if (plus5min) add("+5min")
                if (repetitive) add("SI $surfaceInterval")
            }

            return (listOf(dive, modelText) + extras).joinToString(" · ")
        }

    /**
     * The plan from the most recent Calculate, not yet in the log.
     *
     * The log used to take every calculation automatically, which filled it
     * with the half-dozen throwaway runs it takes to settle on a dive. Keeping
     * is now deliberate.
     */
    private var pendingLog by mutableStateOf<LogEntry?>(null)

    /** A plan is on screen that has not been kept. */
    val canSaveLog: Boolean get() = pendingLog != null

    /**
     * Keep the current plan. Distinct from commitDive(): this records a
     * schedule for later reference and changes nothing, while "Next dive"
     * loads your tissues and changes every plan that follows.
     */
    fun saveToLog() {
        val entry = pendingLog ?: return
        // Compare against the whole log, not just the newest entry. Checking
        // only the first meant a plan you had deleted came straight back the
        // next time you saved the same settings.
        if (log.none { it.text == entry.text }) {
            log.add(0, entry)
            store.save(log)
        }
        pendingLog = null
    }

    fun removeLog(e: LogEntry) {
        log.removeAll { it.id == e.id }
        store.save(log)
    }

    /**
     * Carry the loading from the calculated dive forward, timestamped now.
     * Deliberately explicit: calculating must not commit tissue, or editing and
     * recalculating one dive would compound onto itself.
     */
    fun commitDive() {
        val t = resultTissue ?: return
        baselineTissue = t
        baselineDate = System.currentTimeMillis()
        siActual = ""; si24 = false; si48 = false
        // Consume it. Without this the button stayed live after committing, so
        // "Next dive" sat on screen next to "Residual gas is carried" as though
        // nothing had happened — and pressing it again re-stamped the SAME
        // dive with a fresh timestamp, silently resetting the surface interval
        // to zero while the plan on screen was unchanged.
        resultTissue = null
        saveState()
    }

    /** Declare the diver clean again. */
    fun clearTissues() {
        baselineTissue = null
        baselineDate = 0L
        resultTissue = null
        saveState()
    }

    val canCommit: Boolean get() = resultTissue != null

    fun clearLog() {
        log.clear()
        store.save(log)
    }
}
