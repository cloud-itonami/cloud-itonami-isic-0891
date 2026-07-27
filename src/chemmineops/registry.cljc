(ns chemmineops.registry
  "Pure-function material-extraction + consignment-shipment record
  construction -- an append-only quarry book-of-record draft.

  Like every sibling actor's registry, there is no single international
  reference-number standard for an extraction or shipment record --
  every quarry/jurisdiction assigns its own reference format. This
  namespace does NOT invent one; it builds a jurisdiction-scoped
  sequence number and validates the record's required fields, the
  same honest, non-fabricating discipline `chemmineops.facts` uses.

  `royalty-matches-claim?` is an HONEST reapplication of the SAME
  ground-truth-recompute DISCIPLINE `leathergoods.registry`'s/
  `specialtyrepair.registry`'s own `parts-cost-matches-claim?` and
  `retailops.registry`'s own `sale-total-matches-claim?` establish
  (verify a claimed monetary total against the entity's own recorded
  quantity x unit fields), reapplied to a quarry royalty line rather
  than a repair-parts or retail-sale line -- not claimed as new code,
  though no literal code is shared (different domain).

  This namespace is pure data + pure functions -- no I/O, no network
  call to any real quarry-operations system. It builds the RECORD a
  quarry operator would keep, not the act of extracting material or
  shipping a consignment itself (that is `chemmineops.operation`'s
  `:extraction/extract`/`:consignment/ship`, always human-gated --
  see README `Actuation`)."
  (:require [clojure.string :as str]))

(defn- unsigned-certificate
  "Every certificate this actor produces is UNSIGNED -- signature is
  the quarry operator's act, not this actor's. See README
  `Actuation`."
  [kind subject record-id]
  {"@context" ["https://www.w3.org/ns/credentials/v2"]
   "type" ["VerifiableCredential" kind]
   "credentialSubject" {"id" subject "record" record-id}
   "proof" nil
   "issued_by_registry" false
   "status" "draft-unsigned"})

(defn- zero-pad [n w]
  (let [s (str n)]
    (str (apply str (repeat (max 0 (- w (count s))) "0")) s)))

(def ^:private amount-scale
  "Sub-minor-unit scale used when comparing two money amounts: 1/10000
  of a unit. Coarser than double representation error by many orders of
  magnitude, finer than any distinction a real money carries."
  10000)

(defn- money=
  "Exact-at-money-precision equality for two amounts.

  `==` on raw doubles is NOT the right comparison here: a product or sum
  of decimal quantities is routinely not the double nearest the true
  total, so a CORRECT claim compared false and an entity that was never
  wrong was rejected. Measured across this fleet's recompute shapes,
  20-27% of cent-denominated combinations failed while being right.

  Rounding both sides to `amount-scale` before comparing removes the
  representation error while preserving every distinction the value can
  actually carry. A missing or non-numeric amount never matches:
  un-verifiable is not the same as correct."
  [x y]
  (and (number? x) (number? y)
       (= (Math/round (* amount-scale (double x)))
          (Math/round (* amount-scale (double y))))))

(defn compute-royalty
  "The ground-truth royalty owed for `extraction`'s own `:quantity`
  and `:royalty-rate` -- a single flat quantity x rate calculation,
  not a full tax/deduction royalty engine."
  [{:keys [quantity royalty-rate]}]
  (* (double quantity) (double royalty-rate)))

(defn royalty-matches-claim?
  "Does `extraction`'s own `:claimed-royalty` equal the independently
  recomputed `compute-royalty`? A pure ground-truth check against the
  extraction's own permanent fields -- see ns docstring for why this
  is an honest reapplication of the SAME discipline every sibling
  actor's own cost/total-matching check establishes, not a new
  concept."
  [{:keys [claimed-royalty] :as extraction}]
  (money= claimed-royalty (compute-royalty extraction)))

(defn register-extraction
  "Validate + construct the MATERIAL-EXTRACTION registration DRAFT --
  the quarry operator's own legal act of extracting real material
  from the quarry face. Pure function -- does not touch any real
  quarry-operations system; it builds the RECORD an operator would
  keep. `chemmineops.governor` independently re-verifies the
  extraction's own permit-validity and blast-clearance ground truth,
  and blocks a double-extraction of the same record, before this is
  ever allowed to commit."
  [extraction-id jurisdiction sequence]
  (when-not (and extraction-id (not= extraction-id ""))
    (throw (ex-info "extraction: extraction_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "extraction: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "extraction: sequence must be >= 0" {})))
  (let [extraction-number (str (str/upper-case jurisdiction) "-EXT-" (zero-pad sequence 6))
        record {"record_id" extraction-number
                "kind" "extraction-draft"
                "extraction_id" extraction-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "extraction_number" extraction-number
     "certificate" (unsigned-certificate "MaterialExtraction" extraction-number extraction-number)}))

(defn register-consignment-shipment
  "Validate + construct the CONSIGNMENT-SHIPMENT registration DRAFT --
  the quarry operator's own legal act of shipping a real consignment
  (loadout and haul, triggering royalty accrual). Pure function --
  does not touch any real haul-management system; it builds the
  RECORD an operator would keep. `chemmineops.governor` independently
  re-verifies the extraction's own royalty ground truth, and blocks a
  double-shipment of the same record, before this is ever allowed to
  commit."
  [extraction-id jurisdiction sequence]
  (when-not (and extraction-id (not= extraction-id ""))
    (throw (ex-info "consignment-shipment: extraction_id required" {})))
  (when-not (and jurisdiction (not= jurisdiction ""))
    (throw (ex-info "consignment-shipment: jurisdiction required" {})))
  (when (< sequence 0)
    (throw (ex-info "consignment-shipment: sequence must be >= 0" {})))
  (let [shipment-number (str (str/upper-case jurisdiction) "-SHP-" (zero-pad sequence 6))
        record {"record_id" shipment-number
                "kind" "consignment-shipment-draft"
                "extraction_id" extraction-id
                "jurisdiction" jurisdiction
                "immutable" true}]
    {"record" record "shipment_number" shipment-number
     "certificate" (unsigned-certificate "ConsignmentShipment" shipment-number shipment-number)}))

(defn append [history result]
  (conj (vec history) (get result "record")))
