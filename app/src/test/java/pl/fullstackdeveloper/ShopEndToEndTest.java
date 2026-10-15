package pl.fullstackdeveloper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/*
 Wariant asynchroniczny (domyślny): odpowiedź 202 + AWAITING_PAYMENT, a wynik płatności
 pojawia się "po chwili" - eventual consistency. Test musi poczekać (Awaitility).
 */
@SpringBootTest
@AutoConfigureMockMvc
class ShopEndToEndTest {

    @Autowired
    private MockMvc mockMvc;
    private ShopApi shop;

    @BeforeEach
    void beforeEach() {
        shop = new ShopApi(mockMvc);
    }

    @Test
    void order_is_accepted_and_paid_eventually() throws Exception {
        var cardNumber = shop.cardWithBalance("1000");

        var result = shop.placeOrder(cardNumber, "10", "EUR")
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"));

        var orderId = shop.orderId(result);
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                shop.getOrder(orderId).andExpect(jsonPath("$.status").value("PAID")));
    }

    @Test
    void order_is_rejected_eventually_when_balance_is_insufficient() throws Exception {
        var cardNumber = shop.cardWithBalance("5");

        var orderId = shop.orderId(shop.placeOrder(cardNumber, "5000", "PLN"));

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() ->
                shop.getOrder(orderId)
                        .andExpect(jsonPath("$.status").value("REJECTED"))
                        .andExpect(jsonPath("$.rejectionReason").value("Insufficient balance")));
    }

}
