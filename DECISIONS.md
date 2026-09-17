# Build Decisions

## What was built

- Part 1 artifacts for RC-1 to RC-3 were created and kept as the manual source of truth.
- Part 2 contains the React staff portal and Spring Boot API for search, view, quote, and renewal.
- Part 3 records conflicts, assumptions, unresolved questions, owners, and suspected risks in
  `ASSUMPTIONS.md`.
- Part 4 contains a runnable, dependency-free pipeline in `part4/pipeline.py`.

The Part 4 pipeline performs source intake, applies explicit RC-4 decisions, renders business and
technical artifacts, emits a traceability manifest, generates a withdrawal policy, and validates the
result before accepting it.

## Deliberately skipped

- No external LLM call is required. There are no credentials or model-specific dependencies in the
  repository. The renderer is deterministic so the check can run in CI.
- RC-4 is not wired into the Part 2 application. The brief reserves withdrawal for the generated Part 4
  output, so generated code is an isolated policy artifact rather than an unreviewed production change.
- No payment, refund, authentication, actor identity, cancellation, or permit-number generator was
  invented for RC-4.

## Artifact value

The business flowchart and functional document are useful review artifacts for officers and the client.
The technical flowchart and technical document make endpoint, validation, transaction, and failure
behavior explicit for engineers. The traceability manifest earns its place because it gives the gate a
machine-readable list of requirements and source citations.

For a larger system, I would keep the functional and technical contract but generate the two flowcharts
from the same approved intermediate model instead of maintaining duplicate prose.

## Preventing invention and silent drops

The pipeline refuses to run when required source phrases are absent. Every known requirement has an ID,
source citation, statement, and evidence tokens. The validation gate checks that all IDs remain present,
that each evidence token appears in generated artifacts rather than only in the manifest, and that the
approved endpoint exists.

The gate also rejects a small explicit list of unsupported code fields such as `actor`, `refundAmount`,
and `paymentId`. This is intentionally narrow and explainable for the test slice. A production version
would replace the list with schema and endpoint diff checks against an approved intermediate model.

`python part4/pipeline.py --self-test` mutates generated output twice and proves that the gate catches:

1. a removed 500-character requirement;
2. an invented `actor` field.

## Where the documents were incomplete

Part 3 found that the stories did not define date-range semantics, the official permit-number format,
Draft behavior, or actor-level audit identity. It also found later Finance rules that changed renewal
eligibility and fee calculation. Those decisions are recorded in `ASSUMPTIONS.md` rather than silently
changing the signed-off source files.

## AI tradeoffs

AI was most useful for finding cross-document contradictions and keeping implementation, tests, and
documentation aligned. It was least trustworthy when asked to infer missing business rules from plausible
patterns. The pipeline therefore treats source evidence and explicit decisions as gates, not suggestions.

## Next six hours

- Replace the deterministic renderer with a model adapter that emits the same intermediate manifest.
- Add structural validation for generated Java and API contracts.
- Add human approval checkpoints before generated code can be copied into the application.
- Add more adversarial fixtures for invented endpoints, statuses, fields, and dropped acceptance criteria.

## Surprise

The most consequential gaps were not missing technical details. They were small clarification messages
that changed money, eligibility, and operational audit expectations after the stories were signed off.
