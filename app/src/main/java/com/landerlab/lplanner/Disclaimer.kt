package com.landerlab.lplanner

/**
 * Disclaimer.kt — Lplanner Android v1.1.0
 *
 * Shown by the Info button and reproduced in the documentation of every build.
 * [algorithms] names the models the build actually ships, so Lplanner79 states
 * VVAL-79 rather than VVAL-79. Kept identical in wording to the Swift
 * `Disclaimer` in ZPlannerView.swift — the two must not drift.
 */
object Disclaimer {

    var algorithms: String =
        "A. A. Buhlmann's algorithm, the VVAL-79 algorithm, or the VPM-B algorithm"

    val text: String
        get() = "This generated dive schedule could indirectly kill you and probably has " +
            "bugs. The author does not warrant that it accurately reflects " +
            algorithms + ". This dive schedule is experimental, and you use it at " +
            "your own risk."
}

/**
 * Contribution details, shown in the Info dialog of the F-Droid build only.
 *
 * Not the Play build: Google Play's Payments policy does not allow an app to
 * point users at an external payment method for donations unless it is a
 * registered non-profit, and Play would pull the listing. F-Droid has no such
 * restriction, and neither does the macOS build, which shows the same block
 * (see `infoSheet` in ZPlannerView.swift). The iOS build must not show it —
 * App Review guideline 3.2.2(vi) treats an external donation link as
 * circumventing in-app purchase.
 *
 * Both Android trees ship this file byte-identical, which is what
 * `tools/drift-check.sh` requires; [showInBuild] is what differs at runtime,
 * keyed off the application id, so there is nothing to keep in sync by hand.
 */
object Support {
    const val paypal: String = "landercarlos@hotmail.com"

    /**
     * PayPal.Me handle, without the leading "paypal.me/". Empty = no link, and
     * the Info dialog then shows the address alone.
     *
     * This replaces the `/donate/` endpoint, which PayPal refuses outright in
     * some countries — "Donations aren't supported in this organization's
     * country" — whatever parameters are passed. PayPal.Me is a plain payment
     * link rather than a donation flow, so that restriction does not apply; it
     * needs only a personal account, and the sender types the amount. Claim one
     * free at paypal.me. It cannot be changed or deleted afterwards, so pick
     * the handle deliberately.
     */
    const val paypalHandle: String = "carloselander"

    val paypalUrl: String? =
        if (paypalHandle.isEmpty()) null else "https://paypal.me/$paypalHandle"

    const val linkLabel: String = "Send a contribution with PayPal"

    /** True in the F-Droid build (applicationId com.landerlab.lplanner.fdroid). */
    fun showInBuild(applicationId: String): Boolean = applicationId.endsWith(".fdroid")

    const val heading: String = "Support the developer"

    const val text: String =
        "Lplanner is free and has no adverts, no tracking and no subscription. " +
        "If it has been useful to you, you can send the developer a contribution:"

    val fallback: String =
        if (paypalHandle.isEmpty()) "PayPal, to $paypal" else "or send to $paypal"
}

/**
 * Short how-to shown in the Info dialog, under the disclaimer.
 * Kept word-for-word identical to `Manual` in ZPlannerView.swift.
 */
object Manual {
    const val text: String = """ENTERING A DIVE
Type Depth, Time and O2 % — plus He % for trimix — then press Add >>. Repeat for each level. Tap a level to edit it, use the arrows to reorder, × to remove. Click a level's box to leave it out without deleting it.

CLOSED CIRCUIT
Tap the OC chip so it reads CCR — on a tablet, switch Open to Closed. Set (setpoint) and Sld (Scamahorn slide) then appear beside the mix.

DECO GASES
Click Yes and list the mixes, e.g. 50, 100. The planner picks the richest one allowed by Max PO2 and Max END. The new mix appears in the gas column of the stop where you change on to it. If the switch depth is not a stop, a GasSw row marks it instead. Config can also hold you there for a few extra minutes — see Extended stops.

SETTINGS STRIP
On a phone the settings sit in one strip of chips above the tabs. It folds to a single summary line on the Plan tab so the schedule gets the full screen; the chevron opens or closes it by hand. altGF is a plain on/off here — its two numbers are set in Config.

CONFIG
A plain list of controls. Every setting is explained under CONFIG SETTINGS below.

SURFACE INTERVAL AND RESIDUAL GAS
When you surface, press "Next dive" to carry your inert gas loading forward into the dive you plan next. It is kept when the app is closed and ages with real time. While gas is carried you must state a surface interval — 48 hr, 24 hr or Actual — before Calculate will work.

Always use your exact surface interval time or a shorter duration if you're uncertain about how long to wait between dives.

Press Clear to declare yourself clean again.

READING THE PLAN
Press "Full screen" above the schedule for the plan on its own. The type size is computed to fit the width exactly, so turning the phone sideways makes it bigger, not just wider. A− and A+ override it, Fit returns to the computed size, Sun goes to full brightness for reading in sunlight. The screen is held awake the whole time. Tap once to hide the controls; Back closes it.

LOG
Press Keep above the schedule to file a plan, stored with the dive and settings that produced it. Nothing is logged unless you ask. Keep is not "Next dive": it records a schedule, it does not load your tissues. Swipe an entry to delete it, or press Clear.

SHARE AND PRINT
Share, Print and Info sit at the right of the top bar. Share and Print dim while there is no plan to send.

Print opens the system print dialogue, from where the schedule can go to a printer or be saved as a PDF. It prints in the same monospace type you see on screen, because the columns only line up when every character is the same width.

WARNINGS
Advisories are not printed under the table, to keep the schedule readable on a phone. Read them here and apply them yourself. When the planner refuses to produce a schedule at all, the reason is printed in place of it.

GAS DENSITY
Above 5.2 g/L a bottom mix is denser than ideal; above 6.2 g/L it exceeds the limit given by Anthony & Mitchell, where work of breathing and CO2 retention rise steeply. CO2 retention is itself a risk factor for oxygen toxicity and narcosis. Add helium. For reference, 18/45 at 70 m is 6.4 g/L and 18/50 brings it to 5.9.

LAST STOP AT 6 M
A 6 m last stop works only on 100% oxygen, which delivers zero inspired inert gas at any depth. On air, 32%, 50% or anything else the inspired inert pressure must keep falling to drive off-gassing, so finish the stepped ascent — 4.5 m, 3 m — rather than hanging at 6 m. Check that Config, Last stop matches the gas you will actually be breathing there.

ASCENT RATE
Dive the rate you planned. A schedule computed at 10 m/min is wrong if you ascend at 5, which is what most technical divers actually do: either plan the slower rate or hold to the planned one.

The slow final ascent from the last stop is the exception. The planner ignores it, so taking it slowly is extra decompression rather than missing decompression.

ISOBARIC COUNTERDIFFUSION
Changing the inspired He:N2 ratio sharply off-gasses one inert gas while on-gassing the other. Switching from trimix to EAN50 raises inspired nitrogen to roughly what it was several stops deeper, halting nitrogen off-gassing while helium leaves quickly. That is the accepted trade rather than a fault, but do not compound it with a large nitrogen jump at depth. Note also that ICD names a process, not a single injury: the inner-ear form is a distinct problem with its own literature.

TRIMIX DECO GAS
A 50/50 or 50/25 deco mix removes more nitrogen earlier. It does not remove helium faster — breathing helium slows helium off-gassing, and you carry more of it to the switch onto oxygen. A longer schedule on a trimix deco gas is the model working, not a bug.

OXYGEN EXPOSURE
CNS % and OTUs are printed with every plan. Nothing enforces them — 100% CNS is a limit, not a target.

DAN RECOMMENDATIONS
Divers Alert Network guidance, which sits outside any decompression model and is not enforced by this planner.

Flying after diving. The Time to Fly figure on the plan is the model's own arithmetic — the hours until your tissues tolerate a 10,000 ft cabin. It is not DAN's advice and is usually far shorter. DAN recommends a minimum 12-hour surface interval before flying after a single no-decompression dive, 18 hours after multiple dives or several days of diving, and considerably longer after any dive requiring decompression stops — commonly given as at least 24 hours. Take the longer figure.

Altitude after diving. Driving over a mountain pass is the same problem as flying and is easier to overlook. Apply the same intervals.

Diving at altitude. Arriving and diving the same day means your tissues still hold sea-level nitrogen, which is why Config asks whether you are equilibrated. DAN's guidance is to allow time at altitude before diving where you can; the U.S. Navy puts equilibration at about twelve hours.

Hydration, exertion and thermal stress all affect decompression and none are modelled here. Cold on the deep portion followed by warm shallow stops is the worst combination for gas elimination.

Ascent rate. Keep to the rate you planned. DAN and every training agency give 9–10 m/min as the maximum for the shallow portion.

If you feel unwell after a dive, breathe oxygen and call the DAN emergency line for your region. Symptoms that appear hours later are still decompression illness."""
}

/**
 * Every Config setting, explained. Shown in the Info dialog under the manual.
 * Kept word-for-word identical to `ConfigGuide` in ZPlannerView.swift.
 */
object ConfigGuide {
    const val text: String = """Every setting in Config, in the order the sections appear. Nothing here changes a dive on its own: it changes how the planner computes one.

UNITS
Depths sets the units for depth, altitude, stop distance, END, ascent and descent rates, and the dive levels themselves. Every value already entered is converted when you switch, and the plan is then computed in those units — a 10 ft stop grid is a grid of whole feet. RMVs sets the units for breathing-rate and gas-consumption figures; it follows Depths until you set it yourself, after which it stays where you put it.

ENVIRONMENT
Fresh or salt water changes the depth-to-pressure conversion. O2 Narcotic controls whether oxygen counts as narcotic when calculating equivalent narcotic depths (ENDs).

MODEL
ZHL16-C is the Bühlmann set used here.

VVAL-79 is the U.S. Navy Thalmann EL-DCM (exponential uptake, linear elimination) with the VVal-79 air parameter set behind the Diving Manual Revision 7 air tables. It plans AIR AND NITROX ONLY: the Navy publishes no helium parameters for it, so a dive carrying helium is refused rather than computed.

VPM-B is the Yount/Hoffman varying permeability bubble model in Erik Baker's implementation. It limits the volume of gas released from bubble nuclei rather than the tension dissolved in tissue, which is why it puts the first stop much deeper, especially on helium mixes.

Gradient factors and Conservatism apply to ZHL16-C only; VVAL-79 has neither, and VPM-B has its own conservatism ladder. With gradient factors enabled, Pyle deep stops are disabled — GF Low provides the deep-stop function — and Conservatism is ignored.

VPM-B
Conservatism 0–4 scales both critical radii: a larger nucleus is excited by a smaller gradient, so higher levels give more decompression. Level 0 is Baker's nominal VPM-B and is the setting that reproduces his published reference schedule. The critical radii are the parameter that actually differs between implementations — Baker ships 0.6 and 0.5 microns, Subsurface 0.55 and 0.45. Changing them takes you outside the validated envelope, so leave them alone unless you are deliberately comparing against another planner.

ALTERNATIVE GRADIENT FACTORS
A second GF pair, used instead of the main pair whenever altGF is checked on the main screen. Any values are accepted, low and high independently, and they need not bracket the main pair. 100/100 gives the pure Buhlmann ZHL-16C ceiling and higher goes beyond it. A low GF Low with a high GF High deepens the first stop while keeping the shallow stops short.

NDL CALCULATION
Which gradient factor decides whether a direct, no-stop ascent to the surface is still allowed. GF High is the standard behaviour for ZHL16-C. GF Low is stricter and ends the no-decompression phase earlier.

CONDITIONS
Altitude of the dive site, 0 for sea level. Thinner air means more decompression for the same dive. Equilibrated means your tissues have already off-gassed to match it; the U.S. Navy Diving Manual puts that at about twelve hours at altitude. If you drove up this morning you still carry your sea-level nitrogen and need considerably more decompression, which at 3000 m can double the obligation, so state it honestly. Hours at altitude covers the middle, since the slow compartments are still loaded well after the fast ones have finished. This is equilibration, not acclimatisation: adjusting to the lower oxygen takes far longer and is not modelled here.

Conservatism applies only to ZHL16-C with gradient factors off. It (0-50 %) preloads the compartments with extra inert gas, weighted from the fast compartments (none) to the slow ones (the full percentage), as if a previous dive had been made. Zero is the clean-diver profile.

STOP DEPTHS
Stop distance is the interval between decompression stops — 3 m is the convention, some rebreather divers prefer 6 m. Last stop is the depth of the final stop; some prefer pulling the 10 ft / 3 m stop deeper. Both apply to every schedule, whichever model, gradient factors or deep stops are in use.

DEEP STOPS
Pyle deep stops insert short stops between the bottom and the first normal stop (mean-depth rule, re-run iteratively) to reduce microbubble formation and post-dive fatigue. Pyle stop time is the minutes spent at each generated stop (1–5). Not shown when gradient factors are enabled: GF Low takes over the deep-stop role.

AIR BREAKS
A break is planned when you are breathing oxygen at the last stop depth or shallower, or when CNS reaches the warning threshold on any rich mix. Break after is the oxygen time that earns a break, Break for is its length. The oxygen clock is cumulative: it runs across stop changes and excludes travel, so "Break after 30" means thirty minutes of oxygen wherever it was breathed.

Break gas is the mix you switch to. Left blank the planner takes the leanest mix you carry that is still breathable at that depth, which keeps a hypoxic back gas out of a 3 m break. No break is planned in the last few minutes before surfacing, and none on closed circuit, where the plan advises lowering the setpoint instead.

Navy: the break is gas-exchange dead time. Inert tensions freeze and the stop grows by the break length. This is how the US Navy Air/O2 tables were generated and the only treatment published work validates. Subsurface: the break is an ordinary gas segment integrated on the break gas, physically truer and validated by nobody. CNS and OTU accrue on the break gas in both modes.

TRAVEL GAS
With Travel gas checked, a descent on a hypoxic back gas starts on the leanest mix you carry that is breathable at the surface, and changes to the back gas at the first stop increment where the back gas is safe. It costs no decompression: it only moves the first few metres onto a stage. If no carried mix is breathable at the surface the plan says so and starts on the back gas anyway.

DESCENT AND ASCENT RATES
One range per line: depth1-depth2, rate, in your depth units. List descent ranges shallowest first and ascent ranges deepest first, and leave no gaps — a depth not covered by any range has no rate to travel at. The deepest ascent range must reach at least your deepest level, or the ascent from the bottom has no defined rate. Slow shallow ascent rates are credited to the decompression and can shorten stops or remove them entirely.

DECO SET POINT (CCR) AND SLIDE RATE
Setpoint changes by depth range during CCR deco, one per line, e.g. 80-30, 1.4 — a setpoint of 0 switches to open circuit for that range. Only active on closed circuit: the OC/CCR chip on a phone, the Open/Closed control elsewhere. Slide rate is the PO2 burned off per minute during a Scamahorn Slide: enter a bottom setpoint like 1.2-1.6 to ride the descent PO2 spike down to the setpoint for a deco advantage.

EXTENDED STOPS ON A DECO MIX SWITCH
Extra minutes held at the depth where the planner switches to a deco mix, on top of whatever the model requires. Common practice: settle on the new gas, confirm the analysis and the PO2, and let the switch do some work for you. The amount is chosen by the depth of the switch, in two bands. Switches shallower than 7 m / 23 ft are not extended — the final stop is already long. The extra time off-gasses you, so it does not simply add to the total: the stops above it usually shorten.

DECO GAS LIMITS
The planner auto-selects the deco gas with the highest PO2 that stays within Max PO2 and Max END. Set Max PO2 to 1.6 if you want 100% O2 at the 20 ft / 6 m stop; tune it down to lower CNS exposure at the cost of longer deco. At 1.55 oxygen is held to the 3 m stop instead, which lengthens the schedule and lowers the CNS total.

RMV VALUES
Respiratory Minute Volume for gas-consumption planning, in the RMV units above. Deco is usually lower than Bottom, since you are more at rest hanging on the line. If you don't know your RMV, measure it."""
}
