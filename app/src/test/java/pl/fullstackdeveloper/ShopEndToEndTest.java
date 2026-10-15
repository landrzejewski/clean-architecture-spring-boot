package pl.fullstackdeveloper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 Test E2E całego systemu przez publiczne API (REST). Od kroku 13 moduł app nie widzi klas
 use case'ów ani domeny innych modułów (JPMS), więc test - jak każdy klient - korzysta z HTTP.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShopEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void order_in_foreign_currency_is_paid() throws Exception {
        var cardNumber = cardWithBalance("1000");

        placeOrder(cardNumber, "10", "EUR")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void order_is_rejected_when_balance_is_insufficient() throws Exception {
        var cardNumber = cardWithBalance("5");

        placeOrder(cardNumber, "5000", "PLN")
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReason").value("Insufficient balance"));
    }

    private org.springframework.test.web.servlet.ResultActions placeOrder(final String cardNumber, final String amount, final String currency) throws Exception {
        return mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"description": "Item", "cardNumber": "%s", "amount": %s, "currencyCode": "%s"}
                """.formatted(cardNumber, amount, currency)));
    }

    private String cardWithBalance(final String amount) throws Exception {
        var response = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON).content("""
                        {"currencyCode": "PLN"}
                        """))
                .andReturn().getResponse().getContentAsString();
        var cardNumber = response.replaceAll(".*\"number\":\"(\\d+)\".*", "$1");
        mockMvc.perform(post("/api/cards/" + cardNumber + "/transactions").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "IN", "amount": %s, "currencyCode": "PLN"}
                        """.formatted(amount)))
                .andExpect(status().isCreated());
        return cardNumber;
    }

}
