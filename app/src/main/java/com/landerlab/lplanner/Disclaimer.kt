package com.landerlab.lplanner

/**
 * Disclaimer.kt — Lplanner Android v1.1.0
 *
 * Shown by the Info button and reproduced in the documentation of every build.
 * [algorithms] names the models the build actually ships, so Lplanner79 states
 * VVAL-79 rather than VVAL-18. Kept identical in wording to the Swift
 * `Disclaimer` in ZPlannerView.swift — the two must not drift.
 */
object Disclaimer {

    var algorithms: String =
        "A. A. Buhlmann's algorithm, the VVAL-18 algorithm, or the VPM-B algorithm"

    val text: String
        get() = "This generated dive schedule could indirectly kill you and probably has " +
            "bugs. The author does not warrant that it accurately reflects " +
            algorithms + ". This dive schedule is experimental, and you use it at " +
            "your own risk."
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
Units, water, altitude, model, gradient factors, deep stops, ascent and descent rates, RMVs. Each section carries its own explanation.

SURFACE INTERVAL AND RESIDUAL GAS
When you surface, press "Next dive" to carry your inert gas loading forward into the dive you plan next. It is kept when the app is closed and ages with real time. While gas is carried you must state a surface interval — 48 hr, 24 hr or Actual — before Calculate will work.

Always use your exact surface interval time or a shorter duration if you're uncertain about how long to wait between dives.

Press Clear to declare yourself clean again.

READING THE PLAN
Press "Full screen" above the schedule for the plan on its own. The type size is computed to fit the width exactly, so turning the phone sideways makes it bigger, not just wider. A− and A+ override it, Fit returns to the computed size, Sun goes to full brightness for reading in sunlight. The screen is held awake the whole time. Tap once to hide the controls; Back closes it.

LOG
Press Keep above the schedule to file a plan you want. It is stored with the dive and settings that produced it. Nothing is logged unless you ask — the log used to take every calculation and filled with throwaway runs. Keep has nothing to do with "Next dive": it records a schedule, it does not load your tissues. Swipe an entry to delete it, or press Clear.

SHARE AND PRINT
The three buttons on the right of the top bar are Share, Print and Info. They carry no captions — the icons are unmistakable and the words cost the width that Print needs to fit on a phone. Share and Print stay in place at all times and dim while there is no plan to send, so the bar never changes shape under your thumb.

Print opens the system print dialogue, from where the schedule can go to a printer or be saved as a PDF. It prints in the same monospace type you see on screen, because the columns only line up when every character is the same width.

WARNINGS
To keep the schedule readable on a phone, warnings are not printed under the table. Read them here and apply them yourself — the planner will not stop you.

GAS DENSITY
Above 5.2 g/L a bottom mix is denser than ideal; above 6.2 g/L it exceeds the limit given by Anthony & Mitchell, where work of breathing and CO2 retention rise steeply. CO2 retention is itself a risk factor for oxygen toxicity and narcosis. Add helium. For reference, 18/45 at 70 m is 6.4 g/L and 18/50 brings it to 5.9.

LAST STOP AT 6 M
A 6 m last stop works only on 100% oxygen, which delivers zero inspired inert gas at any depth. On air, 32%, 50% or anything else the inspired inert pressure must keep falling to drive off-gassing, so finish the stepped ascent — 4.5 m, 3 m — rather than hanging at 6 m. Check that Config, Last stop matches the gas you will actually be breathing there.

ASCENT RATE
Dive the rate you planned. Time spent deep on the way up is more gas loading, not less, and a schedule computed at 10 m/min is wrong if you ascend at 5 — which is what most technical divers actually do. Either plan the slower rate or hold to the planned one.

The slow final ascent from the last stop is the exception. The planner ignores it, so taking it slowly is extra decompression rather than missing decompression, and it is fine to do.

ISOBARIC COUNTERDIFFUSION
Changing the inspired He:N2 ratio sharply off-gasses one inert gas while on-gassing the other. Switching from trimix to EAN50 raises inspired nitrogen to roughly what it was several stops deeper, halting nitrogen off-gassing while helium leaves quickly. That is the accepted trade rather than a fault, but do not compound it with a large nitrogen jump at depth. Note also that ICD names a process, not a single injury: the inner-ear form is a distinct problem with its own literature.

TRIMIX DECO GAS
A 50/50 or 50/25 deco mix removes more nitrogen earlier. It does not remove helium faster — breathing helium slows helium off-gassing, and you carry more of it to the switch onto oxygen. A longer schedule on a trimix deco gas is the model working, not a bug.

EXTENDED STOPS
Useful at a gas switch, where the new mix has raised the off-gas gradient. Do not extend in the deepest part of the ascent: ambient pressure is still high there and the extra time loads you.

VVAL-18 ON TRIMIX
The U.S. Navy publishes no helium parameters for this model; the helium handling here is this project's own unvalidated extrapolation, and it begins decompression far shallower on helium than a bubble model does. Use VPM-B, or ZHL16-C with gradient factors, for trimix.

OXYGEN EXPOSURE
CNS % and OTUs are printed with every plan. Nothing enforces them — 100% CNS is a limit, not a target.

DAN RECOMMENDATIONS
Divers Alert Network guidance, which sits outside any decompression model and is not enforced by this planner.

Flying after diving. The Time to Fly figure on the plan is the model's own arithmetic — the hours until your tissues tolerate a 10,000 ft cabin. It is not DAN's advice and is usually far shorter. DAN recommends a minimum 12-hour surface interval before flying after a single no-decompression dive, 18 hours after multiple dives or several days of diving, and considerably longer after any dive requiring decompression stops — commonly given as at least 24 hours. Take the longer figure.

Altitude after diving. Driving over a mountain pass is the same problem as flying and is easier to overlook. Apply the same intervals.

Diving at altitude. Arriving and diving the same day means your tissues still hold sea-level nitrogen, which is why Config asks whether you are acclimatised. DAN's guidance is to allow time at altitude before diving where you can.

Hydration, exertion and thermal stress all affect decompression and none are modelled here. Cold on the deep portion followed by warm shallow stops is the worst combination for gas elimination.

Ascent rate. Keep to the rate you planned. DAN and every training agency give 9–10 m/min as the maximum for the shallow portion.

If you feel unwell after a dive, breathe oxygen and call the DAN emergency line for your region. Symptoms that appear hours later are still decompression illness.

Most of the above follows Ross Hemingway's "Some common practices, myths and mistakes on decompression" at decompression.org. None of it replaces the disclaimer above. Validate every schedule against independent tables or software before diving it."""
}
