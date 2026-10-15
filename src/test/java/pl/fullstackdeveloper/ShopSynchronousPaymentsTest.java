package pl.fullstackdeveloper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

// Ten sam use case, inny adapter portu Payments - wynik znany od razu w odpowiedzi
@SpringBootTest(properties = "orders.payments.mode=sync")
@AutoConfigureMockMvc
class ShopSynchronousPaymentsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void order_is_paid_immediately() throws Exception {
        var shop = new ShopApi(mockMvc);
        var cardNumber = shop.cardWithBalance("1000");

        shop.placeOrder(cardNumber, "10", "EUR").andExpect(jsonPath("$.status").value("PAID"));
    }

}
