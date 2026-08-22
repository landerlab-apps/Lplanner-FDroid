package com.landerlab.lplanner

/**
 * ZPlan.kt — Kotlin API for the ZPlanKit decompression engine. Lplanner Android v1.0.0
 *
 * Mirrors ZPlanKit/Sources/ZPlanKit/ZPlanKit.swift. The native library is the
 * same czplan.c the iOS app compiles, built here by the NDK, so both platforms
 * produce identical schedules from identical input.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  WARNING: Decompression software can get you bent or killed.    │
 * │  This engine is experimental. For trained mixed-gas             │
 * │  decompression divers ONLY. Validate every schedule against     │
 * │  independent tables/software before diving it.                  │
 * └─────────────────────────────────────────────────────────────────┘
 */

class ZPlanException(message: String) : Exception(message)

/** Result of a planning run. */
data class DivePlan(
    /** The classic ZPlan-style text report, ready to display or share. */
    val reportText: String,
    /** Functional dive warnings. Must be rendered in red. */
    val warnings: String,
    /** End-of-dive tissue state, `tissue.dat` format, for the next repetitive dive. */
    val tissueFileText: String,
)

object ZPlan {

    init { System.loadLibrary("zplan") }

    private external fun nativePlan(profile: String, tissue: String?): Array<String?>
    private external fun nativeVersion(): String

    /** Engine version, e.g. "1.7.0". */
    val version: String by lazy { nativeVersion() }

    /**
     * Plan a dive from ZPlan-format `profile.dat` text.
     *
     * @param profile contents of a profile.dat (metric or imperial dialect)
     * @param tissueFile contents of a tissue.dat for repetitive dives, or null for a clean diver
     * @throws ZPlanException on a parse error or planning failure
     */
    @Throws(ZPlanException::class)
    fun plan(profile: String, tissueFile: String? = null): DivePlan {
        val r = nativePlan(profile, tissueFile)
        r[0]?.let { throw ZPlanException(it) }
        return DivePlan(
            reportText = r[1].orEmpty(),
            warnings = r[2].orEmpty(),
            tissueFileText = r[3].orEmpty(),
        )
    }
}
