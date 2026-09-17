# Technical Document: RC-4 Withdrawal

## Endpoint

`POST /api/permits/{permitNumber}/withdrawal`

Request:

```json
{ "reason": "Holder cancelled the booking before the event started." }
```

The server loads the current permit. It ignores any client-supplied status, history, date, or refund
value. It validates that the reason is not blank and has at most 500 characters, then requires
`permit.startDate > today`. A started permit returns `409` because it follows the separate cancellation
process.

The write transaction changes the permit status to `WITHDRAWN` and adds one history event with event
type `WITHDRAWN`, a description containing the supplied reason, and the event timestamp. Both writes
must commit together. The workflow has no payment or refund integration.

## Responses

- `200 OK`: updated permit view.
- `400 Bad Request`: missing, blank, or over-limit reason.
- `404 Not Found`: permit does not exist.
- `409 Conflict`: permit start date is today or earlier.

## Explicit non-goals

There is no refund field, payment call, actor field, cancellation implementation, or new permit-number
generator in this slice.
