package pl.fullstackdeveloper.payments.adapters.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.fullstackdeveloper.payments.api.PaymentResult;
import pl.fullstackdeveloper.payments.api.PaymentsApi;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 Strona dostawcy kontraktu: dla żądania z pliku kontraktu odpowiedź musi mieć dokładnie
 kształt z pliku kontraktu (pliki leżą w artefakcie payments-api, wspólnym dla obu stron).
 */
@WebMvcTest(PaymentsRestController.class)
class PaymentsContractProviderTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PaymentsApi paymentsApi;

    @Test
    void completed_payment_matches_contract() throws Exception {
        when(paymentsApi.pay(any())).thenReturn(new PaymentResult.Completed("8b1f8a62-3f1a-4bd4-9f39-2e5b2d0b8c11"));

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(contract("pay-request.json")))
                .andExpect(status().isOk())
                .andExpect(content().json(contract("pay-response-completed.json"), true));
    }

    @Test
    void rejected_payment_matches_contract() throws Exception {
        when(paymentsApi.pay(any())).thenReturn(new PaymentResult.Rejected("Insufficient balance"));

        mockMvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON).content(contract("pay-request.json")))
                .andExpect(content().json(contract("pay-response-rejected.json"), true));
    }

    static String contract(final String name) throws Exception {
        return new ClassPathResource("contracts/" + name).getContentAsString(StandardCharsets.UTF_8);
    }

}
