# Business Model: Community Quarry and Stone Supply

## Classification
- Repository: `cloud-itonami-0810`
- ISIC Rev.5: `0810` — dimension-stone quarrying and aggregate supply for local construction
- Social impact: local-economy land-safety supply-resilience

## Customer
- local builders, municipalities and material cooperatives needing traceable stone supply

## Offer
- extraction planning, grade and stock records, blast safety gating, loadout and haul, royalty audit

## Revenue
- setup fee per site, monthly operations subscription, haul integration, royalty services

## Trust Controls
- extraction outside permit is blocked; safety gates are auditable; royalty records are immutable
- a robot action the governor refuses is never dispatched to hardware
- every dispatch, hold, approval and disclosure path is auditable
- sensitive operating and personal data stays outside Git
- an unconfirmed blast-safety clearance on a blasting extraction forces a hold, un-overridable, before extraction

## Quarry Governor: implementation notes (`:implemented`)

`quarryops.governor` implements the Trust Controls above faithfully: `extraction-
permit-invalid-violations` (the FLAGSHIP new check, grounded in a 4-jurisdiction
mine-safety/extraction-permit catalog -- the US Mine Act/MSHA, the UK's Quarries
Regulations 1999/HSE, Germany's Bundesberggesetz, and Japan's own 鉱山保安法) directly
enforces "extraction outside permit is blocked"; `blast-safety-clearance-unconfirmed-
violations` (a genuinely new, CONDITIONAL check -- only extractions that themselves
declare `:involves-blasting? true` are gated, since mechanical extraction methods like
diamond-wire cutting have no blast-safety concern) directly enforces "safety gates are
auditable", grounded in a SEPARATE explosives/blast-safety sub-citation (US 30 C.F.R.
Part 56 Subpart E, UK Quarries Regulations 1999 shot-firing provisions/Explosives
Regulations 2014, Germany's Sprengstoffgesetz, Japan's own 火薬類取締法) -- all four
jurisdictions covered honestly; `royalty-mismatch-violations` (an honest reapplication
of the SAME ground-truth-recompute discipline every sibling actor's own cost/total-
matching check establishes) directly enforces "royalty records are immutable".
