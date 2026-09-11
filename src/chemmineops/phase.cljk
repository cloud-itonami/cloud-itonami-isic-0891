(ns chemmineops.phase
  "Phase 0->3 staged rollout for the community-quarry actor.

    Phase 0  read-only        -- no writes, still governor-gated.
    Phase 1  assisted-intake  -- extraction intake allowed, every
                                 write needs human approval.
    Phase 2  assisted-assess  -- adds jurisdiction assessment writes
                                 + robot bench-face/quarry-face
                                 verification-mission writes, still
                                 approval.
    Phase 3  supervised auto  -- governor-clean, high-confidence
                                 `:extraction/intake` (no capital risk
                                 yet) may auto-commit. `:extraction/
                                 extract`/`:consignment/ship` NEVER
                                 auto-commit, at any phase.

  `:extraction/extract`/`:consignment/ship` are deliberately ABSENT
  from every phase's `:auto` set, including phase 3 -- a permanent
  structural fact, not a rollout milestone still to come. Extracting
  real material and shipping a real consignment are the two real-
  world legal/financial acts this actor performs; both are always a
  human quarry operator's call. `chemmineops.governor`'s `:actuation/
  extract-material`/`:actuation/ship-consignment` high-stakes gate
  enforces the same invariant independently -- two layers, not one,
  agree on this. `:robotics/simulate-quarry-face-verification` is
  likewise never auto-eligible, at any phase -- the same posture
  every sibling's screening/verification op has. Like every prior
  sibling's phase 3 `:auto` set, this domain has only ONE member
  (`:extraction/intake`) -- no separate no-capital-risk 'file'
  lifecycle distinct from the extraction itself.")

(def read-ops  #{})
(def write-ops #{:extraction/intake :jurisdiction/assess
                 :robotics/simulate-quarry-face-verification
                 :extraction/extract :consignment/ship})

;; NOTE the invariant: `:extraction/extract`/`:consignment/ship` are
;; members of `write-ops` (governor-gated like any write) but are
;; NEVER members of any phase's `:auto` set below. Do not add them
;; there.
(def phases
  "phase -> {:label .. :writes <ops allowed to write> :auto <ops allowed to
  auto-commit when governor-clean>}."
  {0 {:label "read-only"       :writes #{}                                                                   :auto #{}}
   1 {:label "assisted-intake" :writes #{:extraction/intake}                                                  :auto #{}}
   2 {:label "assisted-assess" :writes #{:extraction/intake :jurisdiction/assess
                                         :robotics/simulate-quarry-face-verification}                        :auto #{}}
   3 {:label "supervised-auto" :writes write-ops
      :auto #{:extraction/intake}}})

(def default-phase 3)

(defn gate
  "Adjust a governor disposition for the rollout phase. Returns
  {:disposition kw :reason kw|nil}.

  - a governor HOLD always stays HOLD (compliance wins).
  - a write op not yet enabled in this phase -> HOLD (:phase-disabled).
  - a write op enabled but not auto-eligible -> ESCALATE (:phase-approval),
    even if the governor was clean.
  - `:extraction/extract`/`:consignment/ship` are never auto-eligible
    at any phase, so they always escalate once the governor clears
    them (or hold if the governor doesn't)."
  [phase {:keys [op]} governor-disposition]
  (let [{:keys [writes auto]} (get phases phase (get phases default-phase))]
    (cond
      (= :hold governor-disposition)       {:disposition :hold :reason nil}
      (contains? read-ops op)              {:disposition governor-disposition :reason nil}
      (not (contains? writes op))          {:disposition :hold :reason :phase-disabled}
      (and (= :commit governor-disposition)
           (not (contains? auto op)))      {:disposition :escalate :reason :phase-approval}
      :else                                {:disposition governor-disposition :reason nil})))

(defn verdict->disposition
  "Map a Quarry Governor verdict to a base disposition before the
  phase gate."
  [verdict]
  (cond (:hard? verdict) :hold
        (:escalate? verdict) :escalate
        :else :commit))
