package pl.fullstackdeveloper.payments.adapters.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test przekrojowy: REST -> use case -> domena -> adapter in-memory
@SpringBootTest
@AutoConfigureMockMvc
class PaymentsRestApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void card_balance_reflects_registered_transactions() throws Exception {
        var location = mockMvc.perform(post("/api/cards").contentType(MediaType.APPLICATION_JSON).content("""
                        {"currencyCode": "PLN"}
                        """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post(location + "/transactions").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "IN", "amount": 300, "currencyCode": "PLN"}
                        """))
                .andExpect(status().isCreated());
        mockMvc.perform(post(location + "/transactions").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "OUT", "amount": 120.50, "currencyCode": "PLN"}
                        """))
                .andExpect(status().isCreated());

        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(179.50))
                .andExpect(jsonPath("$.transactionsCount").value(2));
    }

}
