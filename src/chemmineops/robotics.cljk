(ns chemmineops.robotics
  "Robot-executed bench-face/quarry-face verification -- the concrete,
  actor-level realization of ADR-2607011000's robotics premise (every
  cloud-itonami vertical is designed on the premise that a robot
  performs the physical-domain work; an independent governor gates any
  action before it ever reaches hardware) for THIS actor's own README
  claim ('an extraction robot performs drilling, cutting and loadout
  at the quarry face') -- previously only a declared flag, now a real
  mission dispatched through `chemmineops.governor` before any real
  `:actuation/extract-material` proposal may commit.

  A robot mission (`kotoba.robotics/mission`) walks the extraction
  through three :sense/:actuate steps -- bench-face dimensional
  survey, core-sample quality assay, dust/particulate-emissions scan
  -- built with `kotoba.robotics/action` + `kotoba.robotics/telemetry-
  proof`, and reports an overall :passed? verdict.
  `face-boundary-deviation-out-of-range?` independently re-derives
  that verdict from the extraction's OWN recorded face-boundary-
  deviation fields, never from the mission's self-reported result --
  the SAME 'ground truth, not self-report' discipline `chemmineops.
  registry/royalty-matches-claim?` established for royalty.
  `chemmineops.governor`'s `robotics-simulation-violations` calls this
  ns's independent recheck, never the stored :passed? value, before
  any `:actuation/extract-material` proposal may commit.

  Pure data + pure functions -- no real robot I/O, no network.
  `kotoba.robotics` is itself \"policy, not control\"; this namespace
  simulates what a real robot cell would report, deterministically,
  from the extraction's own recorded fields, so tests and the demo
  run offline exactly like every other sibling namespace in this
  actor."
  (:require [kotoba.robotics :as robotics]))

(def mission-actions
  "The three-step bench-face/quarry-face verification mission every
  extraction walks through before `:actuation/extract-material` is
  proposable. All :sense/:actuate at :none/:low safety -- verification
  sensing on a stationary quarry face, not the moving-equipment
  actuation that is `:actuation/extract-material` itself (always
  :safety-critical -- see `chemmineops.governor`)."
  [{:step :bench-face-dimensional-survey    :kind :sense   :safety :none}
   {:step :core-sample-quality-assay        :kind :actuate :safety :low}
   {:step :dust-particulate-emissions-scan  :kind :sense   :safety :none}])

(defn face-boundary-deviation-out-of-range?
  "Ground-truth check: does `extraction`'s own recorded
  :face-deviation-actual fall outside its own recorded
  [:face-deviation-min :face-deviation-max] bounds? Needs no mission
  run or proposal inspection -- its inputs are permanent fields
  already on the extraction, the same shape `chemmineops.registry/
  royalty-matches-claim?` uses for royalty."
  [{:keys [face-deviation-actual face-deviation-min face-deviation-max]}]
  (and (number? face-deviation-actual) (number? face-deviation-min) (number? face-deviation-max)
       (or (< face-deviation-actual face-deviation-min)
           (> face-deviation-actual face-deviation-max))))

(defn simulate-quarry-face-verification
  "Run the robot bench-face/quarry-face verification mission for
  `extraction-id` (`extraction` is the full extraction record, incl.
  face-deviation-* fields). Returns {:mission .. :actions [{:action ..
  :proof ..} ..] :passed? bool}. Deterministic: :passed? is derived
  from the extraction's OWN recorded face-deviation fields via
  `face-boundary-deviation-out-of-range?`, never invented or
  randomized -- `kotoba.robotics` mandates no network/IO, and a
  repeatable simulation is what makes the governor's independent
  recheck (`simulation-out-of-tolerance?`) meaningful."
  [extraction-id extraction]
  (let [out-of-range? (face-boundary-deviation-out-of-range? extraction)
        reading (if out-of-range? :out-of-tolerance :nominal)
        mission (robotics/mission (str "mission-" extraction-id "-face-verify")
                                   :robot/quarry-face-cell-1
                                   :quarry-face-verification
                                   :boundaries {:station "quarry-face-survey-station"}
                                   :max-steps (count mission-actions))
        actions (mapv (fn [{:keys [step kind safety]}]
                        (let [a (robotics/action (str (:mission/id mission) "-" (name step))
                                                  (:mission/id mission) kind safety
                                                  :params {:step step :extraction-id extraction-id})]
                          {:action a
                           :proof (robotics/telemetry-proof (:mission/id mission) step reading
                                                             :provenance :simulated)}))
                      mission-actions)]
    {:mission mission
     :actions actions
     :passed? (not out-of-range?)}))

(defn simulation-out-of-tolerance?
  "Independent ground-truth recheck for the governor: does
  `extraction`'s OWN current face-deviation fields fall out of range
  right now? Ignores whatever :passed? verdict a prior mission run
  stored -- identical in spirit to `chemmineops.registry/royalty-
  matches-claim?`'s refusal to trust a proposal's self-report."
  [extraction]
  (face-boundary-deviation-out-of-range? extraction))
