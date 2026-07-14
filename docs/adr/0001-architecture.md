# ADR-0001: QuarryOps-LLM ⊣ Quarry Governor architecture

## Status

Accepted. `cloud-itonami-isic-0810` promoted from `:blueprint` to
`:implemented` in the `kotoba-lang/industry` registry.

## Context

`cloud-itonami-isic-0810` publishes an OSS business blueprint for
community quarry and stone supply (dimension-stone quarrying and
aggregate supply). Like every prior actor in this fleet, the
blueprint alone is not an implementation: this ADR records the
governed-actor architecture that promotes it to real, tested code,
following the same langgraph StateGraph + independent Governor +
Phase 0→3 rollout pattern established by `cloud-itonami-isic-6511`
(life insurance) and applied across 88 prior siblings, most recently
`cloud-itonami-isic-4920` (community freight transport).

Unlike `retailops`/4711 and `freightops`/4920, this vertical has no
bespoke domain capability library in `kotoba-lang` to wrap (verified:
no `kotoba-lang/quarry`-style repo exists, and `kotoba-lang/robotics`
is the generic cross-cutting robotics contract every cloud-itonami
vertical already uses, not a domain-specific library for this
vertical). This build returns to self-contained domain logic, the
same pattern the majority of this fleet's actors use.

This blueprint's own `:itonami.blueprint/governor` keyword,
`:quarry-governor`, is grep-verified UNIQUE fleet-wide -- no naming-
collision precedent question, a fresh independent build.

## Decision

### Decision 1: fresh governor identity, no reuse precedent needed

`:quarry-governor` is grep-verified unique across every blueprint.edn
in this fleet. This build follows the SAME governed-actor architecture
as every prior actor, but with its own distinct governor identity.

### Decision 2: dual-actuation shape, SEQUENTIAL on the SAME `extraction` entity

This blueprint's own Trust Controls text ("extraction outside permit
is blocked... royalty records are immutable") and its Offer ("...
loadout and haul, royalty audit") name two real-world acts: extracting
material and shipping a consignment. These apply SEQUENTIALLY to the
SAME `extraction` entity -- extract first, ship later -- matching
`freightops`/4920's own sequential shape (dispatch then settle) rather
than `retailops`/4711's own alternative-kind shape (sale-or-reorder).
`high-stakes` is `#{:actuation/extract-material :actuation/ship-
consignment}`.

### Decision 3: `royalty-matches-claim?` -- an honest reapplication of the ground-truth-recompute discipline

`quarryops.registry/royalty-matches-claim?` (extraction's own claimed
royalty vs. quantity x royalty-rate) applies the SAME discipline
`leathergoods.registry`'s/`specialtyrepair.registry`'s/`retailops.
registry`'s own cost/total-matching checks establish -- verify a
claimed monetary total against the entity's own recorded fields,
independent of proposal inspection. No literal code is shared
(different domain), but the discipline is the same, documented as
such rather than claimed as a novel invention.

### Decision 4: entity and op shape

The primary entity is an `extraction`. Four ops: `:extraction/intake`
(directory upsert, no capital risk), `:jurisdiction/assess` (per-
jurisdiction mine-safety/explosives-blast-safety evidence checklist,
never auto), `:extraction/extract` (POSITIVE, high-stakes), and
`:consignment/ship` (POSITIVE, high-stakes).

### Decision 5: `extraction-permit-invalid?` -- the 76th unconditional-evaluation grounding, the FLAGSHIP genuinely new check

Grep-verified absent fleet-wide (zero hits for `permit-invalid`,
`mining-permit` as a governor check name). Grounded in real mine-
safety/extraction-permit law: the US Mine Act (enforced by MSHA), the
UK's Quarries Regulations 1999 (enforced by the HSE), Germany's
Bundesberggesetz (BBergG, enforced by Landesbergämter), and Japan's
own 鉱山保安法 (Mine Safety Act, enforced by METI's Industrial Safety
Group) -- directly grounded in this blueprint's own text ("extraction
outside permit is blocked"). Evaluated UNCONDITIONALLY on every
`:extraction/extract` (every extraction needs a valid permit).

### Decision 6: `blast-safety-clearance-unconfirmed?` -- the 77th unconditional-evaluation grounding, the EIGHTH conditional variant

Before writing this check, every prior sibling's governor namespace
was grepped for any check function named `blast-safety`, `blast-
clearance` or `explosives` -- zero hits, confirming this is a
genuinely new concept. This is the EIGHTH conditional variant (after
`socialresearch`/7220's, `bizassoc`/9411's, `training`/8549's,
`furniture`/9524's, `specialtyrepair`/9529's, `leathergoods`/9523's
and `ictrepair`/9511's own, at 63rd, 64th, 66th, 67th, 68th, 69th and
71st) -- CONDITIONAL on the extraction's own `:involves-blasting?`
ground truth: mechanical extraction methods (e.g. diamond-wire cutting
for dimension stone) have no blast-safety concern at all, only
extraction methods that actually use explosives do. Grounded in real
explosives/blast-safety law: the US 30 C.F.R. Part 56 Subpart E
(MSHA), the UK's Quarries Regulations 1999 shot-firing provisions /
Explosives Regulations 2014 (HSE), Germany's Sprengstoffgesetz
(SprengG), and Japan's own 火薬類取締法 (Explosives Control Act). Unlike
some prior repair-shop-cluster siblings' own honest single-
jurisdiction gap, ALL FOUR seeded jurisdictions actually have a real
regime here, reported honestly (matching `leathergoods`/9523's own,
`ictrepair`/9511's own, `retailops`/4711's own and `freightops`/4920's
own full-coverage sub-citations).

### Decision 7: dedicated double-actuation-guard booleans

`:extracted?`/`:shipped?` are dedicated booleans on the `extraction`
record, never a single `:status` value -- the same discipline every
prior governor's guards establish, informed by `cloud-itonami-isic-
6492`'s real status-lifecycle bug (ADR-2607071320).

### Decision 8: Store protocol, MemStore + DatomicStore parity

`quarryops.store/Store` is implemented by both `MemStore` (atom-
backed, default for dev/tests/demo) and `DatomicStore` (`langchain.
db`-backed), proven to satisfy the same contract in
`test/quarryops/store_contract_test.clj`.

### Decision 9: no bespoke domain capability lib, and no `blueprint.edn` field-sync fixes needed

Verified explicitly this session: no `kotoba-lang/quarry`-style
bespoke capability library exists (unlike `retailops`/4711's own
`kotoba-lang/retail` and `freightops`/4920's own `kotoba-lang/
logistics`); `kotoba-lang/robotics` (referenced by only 2 other
verticals' READMEs) is the GENERIC cross-cutting robotics contract
every cloud-itonami vertical implicitly uses, not domain-specific to
this vertical. This repo's `blueprint.edn` already had the correct
`:required-technologies` matching the `kotoba-lang/industry`
registry's own entry for `"0810"` exactly -- only the `:maturity`
field itself needed adding (a clean fix, unlike `freightops`/4920's
own self-caught-and-corrected error).

### Decision 10: mock + LLM advisor pair

`quarryops.quarryopsllm` provides `mock-advisor` (deterministic,
default everywhere -- the actor graph and governor contract run
offline) and `llm-advisor` (backed by `langchain.model/ChatModel`,
with a defensive EDN-proposal parser so a malformed LLM response
degrades to a safe low-confidence noop rather than ever auto-
extracting material or auto-shipping a consignment).

## Alternatives considered

- **An unconditional blast-safety-clearance check** (applying to
  every extraction regardless of whether it actually involves
  blasting). Rejected: mechanical extraction methods have no blast-
  safety concern at all -- forcing the check onto every extraction
  would fabricate a requirement.
- **Fabricating a jurisdiction gap** to match the pattern of some
  prior siblings' own single-jurisdiction honesty gap. Rejected: the
  same honesty discipline that forbids fabricating coverage also
  forbids under-reporting it.
- **Checking for a bespoke capability library that doesn't exist.**
  Considered and explicitly ruled out: `kotoba-lang/robotics` is
  generic, not quarry-specific -- this build correctly returns to
  self-contained domain logic rather than forcing a false capability-
  library integration.

## Consequences

- 89th actor in this fleet (88 implemented before this build).
- Establishes two genuinely NEW unconditional-evaluation-discipline
  checks: `extraction-permit-invalid?` (FLAGSHIP, 76th distinct
  application overall) and `blast-safety-clearance-unconfirmed?`
  (77th distinct application overall, the EIGHTH conditional variant).
- `MemStore` ‖ `DatomicStore` parity is proven by
  `test/quarryops/store_contract_test.clj`.
- 39 tests / 176 assertions pass; lint is clean; the demo
  (`clojure -M:dev:run`) walks two clean extraction+shipment
  lifecycles (no-blast, blast-confirmed), plus four HARD-hold
  scenarios, end-to-end.
- `blueprint.edn` required no field-sync fixes this time (already
  correct) -- only the `:maturity` flip itself.

## References

- `cloud-itonami-isic-6511/docs/adr/0001-architecture.md` (origin of
  the general governed-actor architecture pattern)
- `cloud-itonami-isic-4920/docs/adr/0001-architecture.md` (most recent
  prior sibling, template for this ADR's structure)
- Federal Mine Safety and Health Act (Mine Act), 30 U.S.C. §801 et seq.; 30 C.F.R. Part 56 Subpart E (US)
- Quarries Regulations 1999; Explosives Regulations 2014 (UK)
- Bundesberggesetz (BBergG); Sprengstoffgesetz (SprengG) (Germany)
- 鉱山保安法 (Mine Safety Act); 火薬類取締法 (Explosives Control Act) (Japan)
