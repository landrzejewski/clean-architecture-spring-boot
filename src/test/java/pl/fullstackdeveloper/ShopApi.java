package pl.fullstackdeveloper;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Mały klient REST dla testów E2E
final class ShopApi {

    private final MockMvc mockMvc;

    ShopApi(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    String cardWithBalance(final String amount) throws Exception {
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

    ResultActions placeOrder(final String cardNumber, final String amount, final String currency, final String address) throws Exception {
        return mockMvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content("""
                {"description": "Item", "cardNumber": "%s", "amount": %s, "currencyCode": "%s", "address": "%s"}
                """.formatted(cardNumber, amount, currency, address)));
    }

    ResultActions getCard(final String cardNumber) throws Exception {
        return mockMvc.perform(get("/api/cards/" + cardNumber));
    }

    String orderId(final ResultActions placeOrderResult) throws Exception {
        return placeOrderResult.andReturn().getResponse().getContentAsString().replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");
    }

    ResultActions getOrder(final String orderId) throws Exception {
        return mockMvc.perform(get("/api/orders/" + orderId));
    }

}
