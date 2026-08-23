package com.landerlab.lplanner

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * StateStore.kt — Lplanner Android v1.1.0
 *
 * Persists everything the diver typed in: levels, gases and every Config
 * setting. Previously only the plan output survived nothing at all — closing
 * the app discarded the whole dive.
 *
 * Levels and settings are stored together deliberately. A level of "45" means
 * 45 m or 45 ft depending on `depthsMetric`, so restoring dive data without the
 * units in force when it was entered could silently reinterpret a 45 m dive as
 * 45 ft.
 */
class StateStore(context: Context) {

    private val file = File(context.filesDir, "state.json")

    fun load(): PlannerState? {
        if (!file.exists()) return null
        return try {
            val o = JSONObject(file.readText())
            val s = PlannerState()
            s.depthsMetric = o.optBoolean("depthsMetric", s.depthsMetric)
            s.rmvMetric = o.optBoolean("rmvMetric", s.rmvMetric)
            s.saltWater = o.optBoolean("saltWater", s.saltWater)
            s.o2Narcotic = o.optBoolean("o2Narcotic", s.o2Narcotic)
            s.model = o.optString("model", s.model)
            s.vpmConservatism = o.optInt("vpmConservatism", s.vpmConservatism)
            s.vpmRadiusN2 = o.optString("vpmRadiusN2", s.vpmRadiusN2)
            s.vpmRadiusHe = o.optString("vpmRadiusHe", s.vpmRadiusHe)
            s.useGF = o.optBoolean("useGF", s.useGF)
            s.gfLow = o.optString("gfLow", s.gfLow)
            s.gfHigh = o.optString("gfHigh", s.gfHigh)
            s.altGfLow = o.optString("altGfLow", s.altGfLow)
            s.altGfHigh = o.optString("altGfHigh", s.altGfHigh)
            s.extraSlow = o.optBoolean("extraSlow", s.extraSlow)
            s.ndlLow = o.optBoolean("ndlLow", s.ndlLow)
            s.altitude = o.optString("altitude", s.altitude)
            s.altitudeAcclimatised = o.optBoolean("altitudeAcclimatised", s.altitudeAcclimatised)
            s.hoursAtAltitude = o.optString("hoursAtAltitude", s.hoursAtAltitude)
            s.conservatism = o.optDouble("conservatism", s.conservatism)
            s.deepStops = o.optString("deepStops", s.deepStops)
            s.pyleTime = o.optInt("pyleTime", s.pyleTime)
            s.stopDistance = o.optString("stopDistance", s.stopDistance)
            s.lastStop = o.optString("lastStop", s.lastStop)
            s.descentRates = o.optString("descentRates", s.descentRates)
            s.ascentRates = o.optString("ascentRates", s.ascentRates)
            s.decoSetpoints = o.optString("decoSetpoints", s.decoSetpoints)
            s.slideRate = o.optString("slideRate", s.slideRate)
            s.maxPO2 = o.optString("maxPO2", s.maxPO2)
            s.maxEND = o.optString("maxEND", s.maxEND)
            s.bottomRMV = o.optString("bottomRMV", s.bottomRMV)
            s.decoRMV = o.optString("decoRMV", s.decoRMV)
            s.extStopShallow = o.optInt("extStopShallow", s.extStopShallow)
            s.extStopDeep = o.optInt("extStopDeep", s.extStopDeep)
            s.si48 = o.optBoolean("si48", s.si48)
            s.si24 = o.optBoolean("si24", s.si24)
            s.siActual = o.optString("siActual", s.siActual)
            s.decoGasesOn = o.optBoolean("decoGasesOn", s.decoGasesOn)
            s.decoGases = o.optString("decoGases", s.decoGases)
            s.circuitClosed = o.optBoolean("circuitClosed", s.circuitClosed)
            s.plus3m = o.optBoolean("plus3m", s.plus3m)
            s.plus5min = o.optBoolean("plus5min", s.plus5min)
            s.useAltGF = o.optBoolean("useAltGF", s.useAltGF)
            s.baselineTissue = if (o.isNull("baselineTissue")) null else o.optString("baselineTissue")
            s.baselineDate = o.optLong("baselineDate", 0L)

            val arr = o.optJSONArray("levels") ?: JSONArray()
            s.levels = buildList {
                for (i in 0 until arr.length()) {
                    val l = arr.getJSONObject(i)
                    add(
                        DiveLevel(
                            id = l.optString("id"),
                            enabled = l.optBoolean("enabled", true),
                            d = l.optString("d"), t = l.optString("t"),
                            o2 = l.optString("o2"), he = l.optString("he"),
                            set = l.optString("set"), sld = l.optString("sld"),
                        )
                    )
                }
            }
            s
        } catch (e: Exception) {
            // A corrupt file must never stop the planner from starting.
            Log.w("StateStore", "could not read state.json, using defaults", e)
            null
        }
    }

    fun save(s: PlannerState) {
        try {
            val levels = JSONArray()
            s.levels.forEach { l ->
                levels.put(
                    JSONObject()
                        .put("id", l.id).put("enabled", l.enabled)
                        .put("d", l.d).put("t", l.t).put("o2", l.o2)
                        .put("he", l.he).put("set", l.set).put("sld", l.sld)
                )
            }
            val o = JSONObject()
                .put("depthsMetric", s.depthsMetric).put("rmvMetric", s.rmvMetric)
                .put("saltWater", s.saltWater).put("o2Narcotic", s.o2Narcotic)
                .put("model", s.model)
                .put("vpmConservatism", s.vpmConservatism)
                .put("vpmRadiusN2", s.vpmRadiusN2)
                .put("vpmRadiusHe", s.vpmRadiusHe)
                .put("useGF", s.useGF).put("gfLow", s.gfLow).put("gfHigh", s.gfHigh)
                .put("altGfLow", s.altGfLow).put("altGfHigh", s.altGfHigh)
                .put("extraSlow", s.extraSlow).put("ndlLow", s.ndlLow)
                .put("altitude", s.altitude).put("conservatism", s.conservatism)
                .put("altitudeAcclimatised", s.altitudeAcclimatised)
                .put("hoursAtAltitude", s.hoursAtAltitude)
                .put("deepStops", s.deepStops).put("pyleTime", s.pyleTime)
                .put("stopDistance", s.stopDistance).put("lastStop", s.lastStop)
                .put("descentRates", s.descentRates).put("ascentRates", s.ascentRates)
                .put("decoSetpoints", s.decoSetpoints).put("slideRate", s.slideRate)
                .put("maxPO2", s.maxPO2).put("maxEND", s.maxEND)
                .put("bottomRMV", s.bottomRMV).put("decoRMV", s.decoRMV)
                .put("extStopShallow", s.extStopShallow).put("extStopDeep", s.extStopDeep)
                .put("si48", s.si48).put("si24", s.si24).put("siActual", s.siActual)
                .put("decoGasesOn", s.decoGasesOn).put("decoGases", s.decoGases)
                .put("circuitClosed", s.circuitClosed)
                .put("plus3m", s.plus3m).put("plus5min", s.plus5min)
                .put("useAltGF", s.useAltGF)
                .put("levels", levels)
                .put("baselineTissue", s.baselineTissue ?: JSONObject.NULL)
                .put("baselineDate", s.baselineDate)

            val tmp = File(file.parentFile, "state.json.tmp")
            tmp.writeText(o.toString(2))
            if (!tmp.renameTo(file)) { file.writeText(o.toString(2)); tmp.delete() }
        } catch (e: Exception) {
            Log.w("StateStore", "could not write state.json", e)
        }
    }
}

/** Plain holder for everything the diver entered. Mirrors the Swift PlannerState. */
class PlannerState {
    var depthsMetric = true
    var rmvMetric = true
    var saltWater = true
    var o2Narcotic = false
    var model = "c"
    var vpmConservatism = 0
    var vpmRadiusN2 = "0.6"
    var vpmRadiusHe = "0.5"
    var useGF = false
    var gfLow = "30"
    var gfHigh = "85"
    var altGfLow = "90"
    var altGfHigh = "90"
    var extraSlow = false
    var ndlLow = false
    var altitude = "0"
    var altitudeAcclimatised = false
    var hoursAtAltitude = "0"
    var conservatism = 10.0
    var deepStops = "p"
    var pyleTime = 1
    var stopDistance = "3"
    var lastStop = "3"
    var descentRates = "0-100, 15"
    var ascentRates = "70-30, 18\n30-12, 9\n12-0, 3"
    var decoSetpoints = ""
    var slideRate = "0.1"
    var maxPO2 = "1.6"
    var maxEND = "40"
    var bottomRMV = "19"
    var decoRMV = "14"
    /** Extra hold on a deco mix switch, per depth band, 0-10 min. */
    var extStopShallow = 0
    var extStopDeep = 0
    var si48 = false
    var si24 = false
    var siActual = ""
    var decoGasesOn = true
    var decoGases = "50"
    var circuitClosed = false
    var plus3m = false
    var plus5min = false
    var useAltGF = false
    var levels: List<DiveLevel> = emptyList()

    /**
     * Residual inert gas carried between sessions. [baselineTissue] is the
     * loading at the START of the dive being planned, with the wall-clock time
     * it was recorded. Storing the state before the current dive rather than
     * after it is what lets a plan be recalculated without stacking onto itself.
     */
    var baselineTissue: String? = null
    var baselineDate: Long = 0L
}
