# Assumptions and implementation decisions

## Reference data

The technical document did not originally define how the React portal obtains hall, purpose, and status options. The portal must not duplicate officer-facing names as frontend constants.

**Decision:** add `GET /api/reference-data` and load those options from the API. This keeps the hall list extensible and keeps displayed names consistent with permit data.

## Date-range search

The requirements do not state which permit date a date range filters.

**Temporary assumption:** a date range matches permits whose booking period overlaps the inclusive range:

```text
permit.startDate <= to AND permit.endDate >= from
```

This needs BA confirmation before production use.

## Renewal eligibility

The signed-off story says only `ACTIVE` permits may be renewed. Finance later clarified that permits expired within 90 days may also be renewed.

**Decision:** the Finance clarification is used. `ACTIVE` permits and `EXPIRED` permits within 90 days are eligible. Older expired permits and all other statuses are rejected.

## Renewal fee

The story describes an uncapped daily-rate calculation. Finance later clarified the 30-day cap and the zero-fee Council Use exception.

**Decision:** use `dailyRate * min(addedDays, 30)` for chargeable purposes. Council Use renewals have a zero fee and remain `ACTIVE`.
