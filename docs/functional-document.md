# Functional Document: Permit Register

**Product:** Riverside Council community hall booking permits  
**Release:** Staff-facing register, RC-1 to RC-3  
**Audience:** Riverside Council stakeholders and Permits Officers  
**Status:** Functional specification for client review

## 1. Purpose

The permit register gives Permits Officers one place to find a permit, review its details and renewal
history, and renew an eligible permit when a holder wants to keep using a hall.

This release is for council staff. The public booking journey already exists and is not changed here.

## 2. Search the register

An officer can search using any combination of:

- permit number;
- holder name;
- hall;
- booking purpose;
- permit status;
- date range.

All filters are optional. With no filters, the officer sees the full register.

Holder-name searches match part of a name without treating capitalisation as significant. The hall and
purpose controls show full names rather than internal codes.

Results show 10 permits per page and are ordered with the most recently created permits first. Each
result shows the permit number, holder, hall, purpose, status, start date, end date and fee.

The officer can reset all filters and return to the full register. If no permit matches, the screen
shows an empty state with guidance rather than treating it as an error.

### Date-range behavior

For this release, a permit matches when its booking period overlaps the selected inclusive range:

```text
permit starts on or before the selected end date
AND
permit ends on or after the selected start date
```

This interpretation is a temporary assumption and needs confirmation from the BA before production
use.

## 3. View a permit

Selecting a result opens a read-only permit view. The view shows:

- permit number and holder;
- full hall name and district;
- full booking purpose name;
- current status;
- permit fee;
- start and end dates;
- renewal history, when renewals exist.

Nothing on the view changes the permit unless the officer deliberately starts and confirms a renewal.
Returning to the register keeps the officer's current search filters.

## 4. Renew a permit

An officer can start a renewal from the permit view by entering a new end date.

The new end date must be later than the current end date. The officer sees a clear validation message
when it is not.

The following permits can be renewed:

- `ACTIVE` permits;
- `EXPIRED` permits that expired no more than 90 days ago.

Permits that expired more than 90 days ago cannot be renewed. The holder must make a fresh booking.
Permits in any other status cannot be renewed.

### Renewal fee

For a chargeable purpose, the fee is the hall's daily rate multiplied by the number of days added,
with a maximum charge of 30 days:

```text
fee = daily rate x the smaller of added days and 30
```

Council Use bookings are free. Their renewal fee is zero and they do not enter a payment step.

The system shows the calculated fee before saving anything. The officer must confirm the displayed
quote before the renewal is written.

### Confirmation result

After confirmation:

1. the renewal record shows the previous end date, new end date and fee;
2. the permit end date becomes the new end date;
3. a chargeable renewal moves to `AWAITING_PAYMENT`;
4. a zero-fee Council Use renewal remains `ACTIVE`;
5. the change and reason for the change appear in renewal history.

The three changes are treated as one operation. If the operation cannot be completed, the permit,
renewal record and history remain unchanged.

Payment processing is outside this release. Another service handles payment after a permit enters
`AWAITING_PAYMENT`; there is no payment screen here.

## 5. Reference data

The register uses the current hall list and booking-purpose list supplied by the service. Hall names
are not fixed in the portal, so a future hall can be added without changing the screen's code.

The release includes these known status values:

- `ACTIVE`;
- `EXPIRED`;
- `AWAITING_PAYMENT`;
- `WITHDRAWN`.

Draft behavior is not included because the requirements do not define how draft bookings should appear
or transition.

## 6. Scope boundaries

This release does not include:

- withdrawal or cancellation;
- payment processing or payment screens;
- public booking;
- Excel export;
- printing;
- authentication or actor identity;
- remembering an officer's last search;
- Draft-status behavior.

Withdrawal is reserved for the separate RC-4 Part 4 workflow. The later clarification for that workflow
says withdrawal is allowed only before the permit's start date; a started event follows cancellation.

## 7. Open decisions

- The official permit-number format is not confirmed. The sample format is used for this release.
- The date-range overlap rule above needs BA confirmation.
- Actor-level audit history needs a decision from Operations, Platform and Security.
- Search performance needs a production dataset and response-time target before infrastructure choices
  are finalized.

## 8. Acceptance-criteria coverage

| Story | Criterion | Functional behavior |
|---|---|---|
| RC-1.1 | Combined filters | Section 2 |
| RC-1.2 | Optional filters and full register | Section 2 |
| RC-1.3 | Ten per page, newest first | Section 2 |
| RC-1.4 | Result fields | Section 2 |
| RC-1.5 | Reset | Section 2 |
| RC-1.6 | Empty state | Section 2 |
| RC-2.1 | Full permit and renewal history | Section 3 |
| RC-2.2 | Read-only view | Section 3 |
| RC-2.3 | Preserve filters when returning | Section 3 |
| RC-3.1 | Enter a new end date | Section 4 |
| RC-3.2 | New date after current end date | Section 4 |
| RC-3.3 | Renewal eligibility | Section 4 |
| RC-3.4 | Fee and later Finance rules | Section 4 |
| RC-3.5 | Quote before confirmation | Section 4 |
| RC-3.6 | Renewal, permit, status and history changes | Section 4 |
