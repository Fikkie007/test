package generated.rc4;

import java.time.LocalDate;

/** Pure RC-4 validation; repository writes belong in the application service transaction. */
public final class WithdrawalPolicy {
    private WithdrawalPolicy() { }

    public static void validate(String reason, LocalDate startDate, LocalDate today) {
        if (reason == null || reason.isBlank() || reason.length() > 500) {
            throw new IllegalArgumentException("Withdrawal reason is required and must be 500 characters or fewer.");
        }
        if (!startDate.isAfter(today)) {
            throw new IllegalStateException("A permit that has started must use cancellation instead.");
        }
    }
}
