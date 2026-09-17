# Technical document: permit register search, view and renewal

**Product:** Riverside Council Community Hall Booking Permits  
**Release:** Staff-facing register, RC-1 to RC-3  
**Audience:** Engineers implementing the React portal and Spring Boot API  
**Status:** Build specification for this test slice

## 1. Scope

This release supports Permits Officers to:

- search the permit register;
- open a read-only permit view;
- calculate and confirm a renewal.

The public booking portal, payment processing, Excel export, printing, authentication, and withdrawal are outside this document. Withdrawal is RC-4 and is handled separately by the agent demonstration.

The implementation may use an in-memory repository. The application must keep the repository boundary so the data store can be replaced later.

## 2. Architecture

```text
React staff portal
        |
        | JSON over HTTP
        v
Spring Boot REST API
  - PermitController
  - PermitQueryService
  - PermitViewService
  - RenewalService
        |
        v
Repositories
  - PermitRepository
  - HallRepository
  - RenewalRepository
  - PermitHistoryRepository
```

The browser never calculates or trusts a fee. The API reads the current permit and hall rate, validates the requested date, calculates the fee, and repeats those checks when the renewal is confirmed.

## 3. Data model

### Hall

| Field | Type | Rules |
|---|---|---|
| `id` | string | Required, unique internal identifier |
| `name` | string | Required, displayed to officers |
| `district` | string | Required |
| `dailyRate` | decimal | Required, non-negative, currency amount |
| `active` | boolean | Controls whether the hall is available as reference data |

The four supplied halls are seed data. The hall list must not be hardcoded in React because Westfield Hall is expected in Q4.

### Permit

| Field | Type | Rules |
|---|---|---|
| `permitNumber` | string | Required, unique; use the sample format for this slice |
| `holderName` | string | Required |
| `hallId` | string | Required foreign/reference key |
| `purpose` | enum | Includes the purposes below |
| `status` | enum | See status table |
| `startDate` | date | Required |
| `endDate` | date | Required and not before `startDate` |
| `originalFee` | decimal | Non-negative currency amount |
| `createdAt` | timestamp | Required; used for default sorting |
| `version` | integer | Used to detect stale renewal confirmations |

Purpose values:

- `COMMUNITY_EVENT`
- `RELIGIOUS_SERVICE`
- `PRIVATE_FUNCTION`
- `COMMERCIAL_USE`
- `COUNCIL_USE`

The full hall name and purpose name are returned to the portal. Internal codes are not displayed.

### Renewal

| Field | Type | Rules |
|---|---|---|
| `id` | string | Required, unique |
| `permitNumber` | string | Required reference to the permit |
| `previousEndDate` | date | Required snapshot |
| `newEndDate` | date | Required and later than `previousEndDate` |
| `fee` | decimal | Required, non-negative currency amount |
| `createdAt` | timestamp | Required |

### Permit history

| Field | Type | Rules |
|---|---|---|
| `id` | string | Required, unique |
| `permitNumber` | string | Required reference to the permit |
| `eventType` | enum | `RENEWED` for this release |
| `description` | string | Human-readable change summary |
| `occurredAt` | timestamp | Required |

Actor identity is not included because authentication and user identity were not defined in the supplied requirements. This is an open audit requirement, not an invented field.

## 4. Statuses and transitions

| Current status | Action | Condition | Result |
|---|---|---|---|
| `ACTIVE` | Renew | New end date is later | `AWAITING_PAYMENT` if fee > 0; otherwise `ACTIVE` |
| `EXPIRED` | Renew | End date expired no more than 90 days ago and new date is later | `AWAITING_PAYMENT` if fee > 0; otherwise `ACTIVE` |
| `EXPIRED` | Renew | End date expired more than 90 days ago | Rejected with `409` |
| Any other status | Renew | Status is not eligible | Rejected with `409` |
| Any status | View | Permit exists | Read-only response |

`DRAFT` is not implemented in this slice because no draft behaviour or transition rules were supplied.

Payment processing is external. This service only sets `AWAITING_PAYMENT` for a chargeable renewal.

## 5. Search endpoint

### `GET /api/permits`

Returns a paged permit list.

Query parameters:

| Parameter | Type | Required | Behaviour |
|---|---|---|---|
| `permitNumber` | string | No | Exact match |
| `holderName` | string | No | Partial, case-insensitive match |
| `hallId` | string | No | Exact reference-data match |
| `purpose` | string | No | Exact enum match |
| `status` | string | No | Exact status match |
| `from` | date | No | Inclusive date-range boundary |
| `to` | date | No | Inclusive date-range boundary |
| `page` | integer | No | Zero-based, default `0` |
| `size` | integer | No | Default `10`; maximum `100` |
| `sort` | string | No | Fixed default `createdAt,desc` |

Filters are combined with AND semantics. With no filters, the complete register is returned in pages.

For this slice, a date range matches permits whose booking period overlaps the inclusive range:

```text
permit.startDate <= to AND permit.endDate >= from
```

This overlap interpretation is an implementation assumption because the source requirements do not define which permit date the range filters.

Example response:

```json
{
  "content": [
    {
      "permitNumber": "P-2026-0001",
      "holderName": "Amelia Tan",
      "hall": "Riverside Community Hall",
      "purpose": "Community Event",
      "status": "ACTIVE",
      "startDate": "2026-06-01",
      "endDate": "2026-06-14",
      "fee": 1680.00
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

An empty result is a successful response with `content: []`, not an error.

The Reset control is client-side behaviour: clear all query parameters and request the default list again.

## 6. Permit detail endpoint

### `GET /api/permits/{permitNumber}`

Returns the full read-only permit view.

Example response:

```json
{
  "permitNumber": "P-2026-0001",
  "holderName": "Amelia Tan",
  "hall": {
    "id": "riverside-community-hall",
    "name": "Riverside Community Hall",
    "district": "Riverside"
  },
  "purpose": "Community Event",
  "status": "ACTIVE",
  "startDate": "2026-06-01",
  "endDate": "2026-06-14",
  "fee": 1680.00,
  "renewalHistory": []
}
```

Responses:

- `200 OK`: permit and renewal history returned.
- `404 Not Found`: permit number does not exist.

This endpoint performs no writes. The portal keeps the current search filters when navigating back to the result grid.

## 7. Renewal quote endpoint

### `POST /api/permits/{permitNumber}/renewal-quote`

Calculates a quote without changing any stored data.

Request:

```json
{
  "newEndDate": "2026-06-28"
}
```

Processing order:

1. Load the permit and hall daily rate.
2. Return `404` if the permit does not exist.
3. Reject with `400` if `newEndDate` is not after the current end date.
4. Reject with `409` if the permit is not eligible for renewal.
5. Calculate the number of added days.
6. Apply the fee rule.
7. Return the quote for officer confirmation.

Fee calculation:

```text
addedDays = daysBetween(currentEndDate, newEndDate)

if purpose == COUNCIL_USE:
    fee = 0
else:
    fee = hall.dailyRate * min(addedDays, 30)
```

The quote response must identify whether payment is required:

```json
{
  "quoteId": "opaque-quote-id",
  "permitNumber": "P-2026-0001",
  "previousEndDate": "2026-06-14",
  "newEndDate": "2026-06-28",
  "addedDays": 14,
  "fee": 1680.00,
  "paymentRequired": true
}
```

The fee is calculated by the API and displayed before confirmation. Cancelling or navigating away after this response performs no write.

## 8. Renewal confirmation endpoint

### `POST /api/permits/{permitNumber}/renewals`

Confirms and persists a renewal.

Request:

```json
{
  "newEndDate": "2026-06-28",
  "quoteId": "opaque-quote-id"
}
```

`quoteId` is an opaque correlation value returned with the quote. It is not trusted as proof of the fee or eligibility. The server must recalculate the fee and repeat all eligibility checks. Any fee or status value supplied by the client is ignored.

Write transaction:

1. Reload the permit and hall rate.
2. Revalidate the new date and renewal eligibility.
3. Recalculate the fee.
4. Create a renewal record with the previous end date, new end date, and fee.
5. Update the permit end date.
6. Set status to `AWAITING_PAYMENT` when fee is greater than zero; otherwise retain or set `ACTIVE`.
7. Create a `RENEWED` history record.
8. Increment the permit version.
9. Commit all writes together.

If any write fails, none of the renewal record, permit update, or history record is retained.

Success response:

- `200 OK`: return the updated permit view, including renewal history.

Failure responses:

- `404 Not Found`: permit does not exist.
- `400 Bad Request`: new end date is invalid.
- `409 Conflict`: permit is not eligible, or its version changed since the quote was displayed.

## 9. Error response shape

All expected validation and business errors use the same shape:

```json
{
  "code": "RENEWAL_DATE_NOT_AFTER_CURRENT_END",
  "message": "The new end date must be after 2026-06-14.",
  "field": "newEndDate"
}
```

The React portal displays `message` near the relevant action. It must not expose stack traces or raw persistence errors.

## 10. Frontend behaviour

- The register screen sends filters and paging state to `GET /api/permits`.
- Loading, empty, error, and populated states are distinct.
- Hall and purpose names come from the API response, not client-side code tables.
- Selecting a row opens the detail route without clearing search filters.
- The detail screen is read-only until the officer starts a renewal.
- The renewal form submits only a new end date to request a quote.
- The fee confirmation action is unavailable until a successful quote is displayed.
- Confirmation refreshes the permit detail after a successful renewal.
- A failed confirmation leaves the current permit view unchanged and shows the API error.

## 11. Seed data requirements

Seed at least:

- one active chargeable permit that can be renewed;
- one active Council Use permit with a zero-fee renewal;
- one expired permit within 90 days that can be renewed;
- one expired permit more than 90 days old that is rejected;
- one permit with existing renewal history;
- one permit in `AWAITING_PAYMENT`;
- one permit with `WITHDRAWN` status for search and view coverage.

The supplied sample records are illustrative. Additional records are required to demonstrate the business rules.

## 12. Explicitly unresolved items

- Official permit numbering is unresolved. The sample `P-2026-0001` format is used for this slice and the generator is isolated for later replacement.
- The requested start-date sorting is not implemented because it conflicts with the signed-off newest-first rule and was not approved by the BA.
- Excel export is out of scope for this release.
- Actor-level audit history needs a decision from Operations and the platform/security owner.
- Draft status needs confirmed behaviour before it can be added to the register.
- The date-range overlap rule is an implementation assumption and should be confirmed by the BA.

## 13. Traceability

| Requirement | Implementation section |
|---|---|
| RC-1.1 filters | Section 5 |
| RC-1.2 optional filters | Section 5 |
| RC-1.3 paging and newest first | Section 5 |
| RC-1.4 result fields | Section 5 |
| RC-1.5 reset | Section 5 and Section 10 |
| RC-1.6 empty state | Section 5 and Section 10 |
| RC-2.1 full permit and renewal history | Section 6 |
| RC-2.2 read-only view | Section 6 and Section 10 |
| RC-2.3 preserve search filters | Section 6 and Section 10 |
| RC-3.1 new end date | Section 7 |
| RC-3.2 date validation | Section 7 |
| RC-3.3 renewal eligibility | Section 4 and Section 7 |
| RC-3.4 renewal fee | Section 7 |
| RC-3.5 confirmation before saving | Section 7 and Section 8 |
| RC-3.6 renewal, permit, status, and history writes | Section 8 |
| Finance fee cap | Section 7 |
| Finance Council Use rule | Section 3 and Section 7 |
| Finance 90-day rule | Section 4 and Section 7 |
| Full hall and purpose names | Section 3 and Section 10 |
| Payment outside this service | Section 4 and Section 8 |
| Halls must not be hardcoded | Section 3 |
