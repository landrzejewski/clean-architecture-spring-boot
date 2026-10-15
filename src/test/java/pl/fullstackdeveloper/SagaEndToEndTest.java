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
 Saga od początku do końca przez REST: płatność -> wysyłka -> potwierdzenie,
 a przy błędzie wysyłki kompensacja (zwrot środków) i anulowanie zamówienia.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SagaEndToEndTest {

    @Autowired
    private MockMvc mockMvc;
    private ShopApi shop;

    @BeforeEach
    void beforeEach() {
        shop = new ShopApi(mockMvc);
    }

    @Test
    void order_is_paid_shipped_and_confirmed() throws Exception {
        var cardNumber = shop.cardWithBalance("1000");

        var orderId = shop.orderId(shop.placeOrder(cardNumber, "100", "PLN", "Warszawa, Polna 1").andExpect(status().isAccepted()));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> shop.getOrder(orderId)
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.shipmentId").isNotEmpty()));
        shop.getSaga(orderId)
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedSteps[0]").value("CHARGE_PAYMENT"))
                .andExpect(jsonPath("$.completedSteps[1]").value("SCHEDULE_SHIPMENT"));
        shop.getCard(cardNumber).andExpect(jsonPath("$.balance").value(900.0));
    }

    @Test
    void undeliverable_order_is_refunded_and_cancelled() throws Exception {
        var cardNumber = shop.cardWithBalance("1000");

        var orderId = shop.orderId(shop.placeOrder(cardNumber, "100", "PLN", "Antarctica, McMurdo Station"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> shop.getOrder(orderId)
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.rejectionReason").value("Address is outside of the delivery area")));
        shop.getSaga(orderId).andExpect(jsonPath("$.status").value("COMPENSATED"));
        // kompensacja semantyczna: karta została obciążona, a potem uznana tą samą kwotą
        shop.getCard(cardNumber)
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.transactionsCount").value(3));
    }

    @Test
    void order_without_funds_is_rejected_without_compensation() throws Exception {
        var cardNumber = shop.cardWithBalance("10");

        var orderId = shop.orderId(shop.placeOrder(cardNumber, "100", "PLN", "Warszawa, Polna 1"));

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> shop.getOrder(orderId)
                .andExpect(jsonPath("$.status").value("REJECTED")));
        shop.getSaga(orderId).andExpect(jsonPath("$.completedSteps").isEmpty());
    }

}
