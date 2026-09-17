# RC-4 Technical Flow

```text
React permit view
  -> POST /api/permits/{permitNumber}/withdrawal { reason }
  -> WithdrawalController
  -> WithdrawalService
       -> load permit
       -> validate reason and permit.startDate > today
       -> create WITHDRAWN permit state
       -> write withdrawal history
       -> commit both writes
  -> 200 updated permit view
```

Failure paths:

- `404` when the permit does not exist.
- `400` when the reason is blank or longer than 500 characters.
- `409` when the permit has already started.
- No payment or refund operation is called.
