# Assumptions and Decisions

This record explains how the signed-off stories and later clarification messages were reconciled for
the Part 2 implementation. The source requirements remain unchanged.

## Decisions Made

### Renewal eligibility

- `requirements/stories.md`, RC-3.3: only `ACTIVE` permits may be renewed.
- `requirements/clarifications.md`, Finance email, 12 May: permits expired for no more than 90 days
  may also be renewed; permits expired for more than 90 days may not.

**Decision:** use the later Finance clarification. `ACTIVE` permits and `EXPIRED` permits within 90
days are eligible. Older expired permits and every other status are rejected with `409`.

**Visible in:** `docs/technical-document.md` sections 5 and 8; `RenewalService`; seed data and API
tests.

### Renewal fee

- `requirements/stories.md`, RC-3.4: daily rate multiplied by every added day.
- `requirements/clarifications.md`, Finance email, 12 May: charge no more than 30 days of the daily
  rate, regardless of the extension length.
- The same Finance email says `COUNCIL_USE` bookings are free and do not enter payment.

**Decision:** use the later Finance rule: `dailyRate * min(addedDays, 30)` for chargeable purposes;
`COUNCIL_USE` has a zero fee and remains `ACTIVE` after confirmation.

**Visible in:** `docs/technical-document.md` sections 5, 8, and 9; `RenewalService`; fee-cap and
Council Use tests.

### Default register sorting

- `requirements/stories.md`, RC-1.3: most recently created first.
- `requirements/clarifications.md`, Ops chat, 19 May: officers would prefer start date, soonest first;
  the BA says it will be raised with the team but does not approve the change.

**Decision:** retain the signed-off `createdAt,desc` behavior. A preference in an unresolved chat
message does not override an approved acceptance criterion.

**Ask:** BA and Product/Ops should confirm whether a future release changes the signed-off sort.

**Visible in:** `docs/technical-document.md` sections 6 and 13; `PermitService.search`.

### Purpose values

- `requirements/stories.md` lists four purposes and does not list Council Use.
- `requirements/clarifications.md`, Finance email, 12 May: Council Use bookings are free and must not
  enter payment.

**Decision:** add `COUNCIL_USE` as a clarification-backed value for this slice. It is returned by API
reference data and its renewal rule is implemented on the server.

**Visible in:** `Purpose`, `ReferenceDataController`, `RenewalService`, and seed data.

## Unresolved Requirements

### Permit numbering

- `requirements/stories.md` samples use `P-2026-0001`.
- `requirements/clarifications.md`, BA email, 28 May: Finance uses `RC/2026/0001`, another team has
  a third format, and the BA has no confirmed answer.

**Temporary assumption:** use the sample `P-2026-0001` format for this slice. Permit numbers are
treated as opaque identifiers; no new numbering generator is implemented.

**Ask:** BA and the legacy-system owner must confirm the official external numbering format before
production or any new permit-generation workflow.

**Visible in:** `docs/technical-document.md` section 13; seed data only.

### Date-range meaning

The stories specify a date range but do not say whether it filters start date, end date, or booking
overlap.

**Temporary assumption:** a permit matches when its booking period overlaps the inclusive range:

```text
permit.startDate <= to AND permit.endDate >= from
```

**Ask:** BA should confirm the intended date semantics using officer examples before production use.

**Visible in:** `docs/technical-document.md` sections 6 and 13; `PermitService.SearchCriteria`.

### Draft status

- `requirements/clarifications.md`, BA email, 28 May: Draft exists for bookings not yet approved.
- No draft behavior, search semantics, view behavior, or transitions are defined in the signed-off
  stories.

**Temporary assumption:** Draft is not part of the Part 2 register and cannot be renewed. It is not
  added to the implementation until its behavior is approved.

**Ask:** BA and Product must define Draft lifecycle and register behavior.

**Visible in:** `docs/technical-document.md` sections 5 and 13; no Draft status in the Part 2 code.

### Actor-level audit history

The Hall Supervisor reports that officers need to know who changed a permit during handover. The
stories define permit history but do not define actor identity, authentication, or an identity source.

**Temporary assumption:** retain system event history for renewals, but do not invent an actor field
or authentication model in Part 2.

**Ask:** Operations, Platform, and Security must define identity, authorization, retention, and audit
requirements.

**Visible in:** `docs/technical-document.md` section 4; renewal history contains the event but no actor.

## Explicitly Out Of Scope

These requests are recorded rather than silently dropped:

- Excel export: raised by Ops, but the BA says it is probably not this release.
- Printing: raised by the Hall Supervisor; no approved requirement or design exists.
- Remembering the last search: described as a nice-to-have with no acceptance criterion.
- Payment processing or a payment screen: the permit only moves to `AWAITING_PAYMENT`; another system
  handles payment.
- Withdrawal: RC-4 is reserved for Part 4 and is not implemented in the Part 2 manual slice.

The withdrawal clarification is still recorded for Part 4: a permit may be withdrawn only before its
start date; once started, it follows a separate cancellation process. A withdrawal reason is required
and is limited to 500 characters.

## Suspected Risks

### Search performance

The Hall Supervisor reports that the old search is slow, but no dataset size, response-time target,
or indexing requirement is supplied. The Part 2 in-memory implementation scans and sorts the current
register for each request. This is adequate for the build test, not evidence that production search
performance is solved.

**Ask:** Product and Architecture should provide a target dataset, response-time objective, and query
profile before selecting indexes or a search strategy.

### Data persistence

The technical document permits in-memory storage for this slice. The current renewal transaction is
serialized in the service layer and rolls back the three in-memory writes on failure. This is a
temporary substitute for a database transaction, not a production persistence design.

**Ask:** Architecture should select the production store and transaction/concurrency strategy before
deployment.
