package com.riverside.permits;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import com.riverside.permits.service.PermitService;
import com.riverside.permits.service.RenewalService;
import java.time.LocalDate;

@SpringBootTest
@AutoConfigureMockMvc
class PermitApiTests {
    @Autowired MockMvc mvc;
    @Autowired PermitService service;
    @Autowired RenewalService renewalService;

    @Test void referenceDataProvidesOfficerLabels() throws Exception {
        mvc.perform(get("/api/reference-data")).andExpect(status().isOk())
                .andExpect(jsonPath("$.halls[0].name").isString())
                .andExpect(jsonPath("$.purposes[?(@.value == 'COUNCIL_USE')].name").value("Council Use"))
                .andExpect(jsonPath("$.statuses[?(@.value == 'AWAITING_PAYMENT')].name").value("Awaiting payment"));
    }

    @Test void searchAndDetailExposeNamesAndHistory() throws Exception {
        mvc.perform(get("/api/permits").param("holderName", "amelia"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.content[0].hall").value("Riverside Community Hall"));
        mvc.perform(get("/api/permits/P-2026-0001")).andExpect(status().isOk())
                .andExpect(jsonPath("$.history[0].eventType").value("RENEWED"));
    }

    @Test void confirmationPersistsRenewalAndChangesStatus() throws Exception {
        LocalDate newEnd = LocalDate.now().plusDays(60);
        RenewalService.Quote quote = renewalService.quote("P-2026-0001", newEnd);
        mvc.perform(post("/api/permits/P-2026-0001/renewals").contentType("application/json")
                .content("{\"newEndDate\":\"" + newEnd + "\",\"quoteId\":\"" + quote.quoteId() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"))
                .andExpect(jsonPath("$.renewalHistory[0].newEndDate").value(newEnd.toString()));
    }

    @Test void quoteCapsChargeableFee() throws Exception {
        mvc.perform(post("/api/permits/P-2026-0002/renewal-quote").contentType("application/json")
                .content("{\"newEndDate\":\"2099-01-01\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.addedDays").isNumber())
                .andExpect(jsonPath("$.fee").value(2850.0)).andExpect(jsonPath("$.paymentRequired").value(true));
    }

    @Test void councilUseIsFreeAndOldExpiredPermitIsRejected() throws Exception {
        mvc.perform(post("/api/permits/P-2026-0010/renewal-quote").contentType("application/json")
                .content("{\"newEndDate\":\"2099-01-01\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.fee").value(0)).andExpect(jsonPath("$.paymentRequired").value(false));
        mvc.perform(post("/api/permits/P-2026-0003/renewal-quote").contentType("application/json")
                .content("{\"newEndDate\":\"2099-01-01\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RENEWAL_EXPIRED_TOO_LONG"));
    }

    @Test void confirmationRejectsDateDifferentFromQuote() throws Exception {
        LocalDate quotedEnd = LocalDate.now().plusDays(30);
        RenewalService.Quote quote = renewalService.quote("P-2026-0002", quotedEnd);
        mvc.perform(post("/api/permits/P-2026-0002/renewals").contentType("application/json")
                .content("{\"newEndDate\":\"" + quotedEnd.plusDays(1) + "\",\"quoteId\":\"" + quote.quoteId() + "\"}"))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("RENEWAL_VERSION_CONFLICT"));
    }
}
