# Functional Document: RC-4 Withdrawal

## Purpose

Permits Officers can withdraw a permit when the holder asks them to, provided the permitted event has
not started. The withdrawal reason is retained in the permit history. This workflow does not refund
money.

## Officer journey

1. Open a permit from the register.
2. Choose **Withdraw permit**.
3. Enter a mandatory reason of no more than 500 characters.
4. If the event has started, the system rejects the action and directs the officer to cancellation.
5. Review and confirm the withdrawal.
6. The permit shows as withdrawn and the reason appears in its history.

## Acceptance criteria coverage

| ID | Behavior | Evidence |
|---|---|---|
| RC-4.1 | Withdrawal starts from the permit view. | `POST /api/permits/{permitNumber}/withdrawal` |
| RC-4.2 | Reason is required and capped at 500 characters. | `WithdrawalRequest.reason` |
| RC-4.3 | Status becomes `WITHDRAWN`; reason is recorded in history. | `WithdrawalPolicy`, withdrawal history event |
| RC-4.4 | No refund is initiated. | No payment/refund endpoint or field |
| RC-4.C1 | Only permits not yet started can be withdrawn. | `startDate > today`; otherwise cancellation |
