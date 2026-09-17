# RC-4 Withdrawal: Officer Flow

```text
Open a permit
      |
Choose "Withdraw permit"
      |
Is the event still in the future?
   /        no         yes
 |           |
Explain      Enter a reason
withdrawal   (required, up to 500 characters)
is not       |
available    Confirm withdrawal
                 |
          Permit is withdrawn
          and the reason is recorded
```

Withdrawal releases the permit from this workflow. It does not issue a refund. If the event has
already started, the officer must use the separate cancellation process.
