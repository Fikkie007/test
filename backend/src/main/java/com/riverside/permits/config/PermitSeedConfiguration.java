package com.riverside.permits.config;

import com.riverside.permits.domain.*;
import com.riverside.permits.repository.*;
import java.math.BigDecimal;
import java.time.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermitSeedConfiguration {
    @Bean
    CommandLineRunner seed(PermitRepository permits, HallRepository halls, RenewalRepository renewals,
            PermitHistoryRepository history, Clock clock) {
        return args -> {
            halls.save(new Hall("riverside-community-hall", "Riverside Community Hall", "Riverside", new BigDecimal("120.00"), true));
            halls.save(new Hall("eastgate-pavilion", "Eastgate Pavilion", "Eastgate", new BigDecimal("95.00"), true));
            halls.save(new Hall("northbrook-function-room", "Northbrook Function Room", "Northbrook", new BigDecimal("80.00"), true));
            halls.save(new Hall("southbank-assembly-hall", "Southbank Assembly Hall", "Southbank", new BigDecimal("150.00"), true));
            Instant now = Instant.now(clock);
            LocalDate today = LocalDate.now(clock);
            add(permits, "P-2026-0001", "Amelia Tan", "riverside-community-hall", Purpose.COMMUNITY_EVENT, PermitStatus.ACTIVE, today.minusDays(10), today.plusDays(4), new BigDecimal("1680.00"), now.minusSeconds(7));
            add(permits, "P-2026-0002", "Grace Fellowship", "eastgate-pavilion", Purpose.RELIGIOUS_SERVICE, PermitStatus.ACTIVE, today, today.plusDays(29), new BigDecimal("2850.00"), now.minusSeconds(6));
            add(permits, "P-2026-0003", "Devi Ramasamy", "northbrook-function-room", Purpose.PRIVATE_FUNCTION, PermitStatus.EXPIRED, today.minusDays(120), today.minusDays(114), new BigDecimal("560.00"), now.minusSeconds(5));
            add(permits, "P-2026-0006", "Marcus Oyelaran", "southbank-assembly-hall", Purpose.PRIVATE_FUNCTION, PermitStatus.AWAITING_PAYMENT, today.plusDays(10), today.plusDays(11), new BigDecimal("300.00"), now.minusSeconds(4));
            add(permits, "P-2026-0008", "Priya Nair", "riverside-community-hall", Purpose.PRIVATE_FUNCTION, PermitStatus.WITHDRAWN, today.minusDays(20), today.minusDays(18), new BigDecimal("360.00"), now.minusSeconds(3));
            add(permits, "P-2026-0010", "Council Events", "eastgate-pavilion", Purpose.COUNCIL_USE, PermitStatus.ACTIVE, today.minusDays(2), today.plusDays(5), BigDecimal.ZERO, now.minusSeconds(2));
            add(permits, "P-2026-0011", "Lapsed Arts Group", "southbank-assembly-hall", Purpose.COMMUNITY_EVENT, PermitStatus.EXPIRED, today.minusDays(40), today.minusDays(5), new BigDecimal("1500.00"), now.minusSeconds(1));
            renewals.save(new Renewal("seed-renewal-1", "P-2026-0001", today.minusDays(20), today.minusDays(10), new BigDecimal("120.00"), now.minusSeconds(8)));
            history.save(new PermitHistory("seed-history-1", "P-2026-0001", "RENEWED", "Renewed to " + today.minusDays(10), now.minusSeconds(8)));
        };
    }
    private static void add(PermitRepository r, String n, String holder, String hall, Purpose purpose, PermitStatus status, LocalDate start, LocalDate end, BigDecimal fee, Instant created) {
        r.save(new Permit(n, holder, hall, purpose, status, start, end, fee, created, 0));
    }
}
