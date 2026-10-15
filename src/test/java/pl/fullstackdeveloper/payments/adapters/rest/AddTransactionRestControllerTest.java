package pl.fullstackdeveloper.payments.adapters.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.common.cqrs.Bus;
import pl.fullstackdeveloper.payments.application.CardNotFoundException;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.InsufficientBalanceException;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.TransactionId;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

/*
 Test adaptera w izolacji: tylko warstwa web, szyna komend zastąpiona mockiem.
 Sprawdzamy kontrakt HTTP: mapowanie JSON -> komenda, kody statusu, Problem Details.
 */
@WebMvcTest(AddTransactionRestController.class)
@Import(CardRestMapper.class)
class AddTransactionRestControllerTest {

    private static final String CARD_NUMBER = "1111222233334444";
    private static final String URL = "/api/cards/" + CARD_NUMBER + "/transactions";
    private static final String VALID_REQUEST = """
            {"type": "OUT", "amount": 100, "currencyCode": "PLN"}
            """;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private Bus bus;

    @Test
    void valid_request_is_mapped_to_command() throws Exception {
        var transactionId = new TransactionId(UUID.randomUUID());
        when(bus.send(any(AddTransactionCommand.class))).thenReturn(transactionId);

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost" + URL + "/" + transactionId.value()))
                .andExpect(jsonPath("$.id").value(transactionId.value().toString()));

        verify(bus).send(new AddTransactionCommand(new CardNumber(CARD_NUMBER), Money.of("100", "PLN"), PAYMENT));
    }

    @Test
    void invalid_request_is_rejected_before_reaching_use_case() throws Exception {
        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content("""
                        {"type": "OUT", "amount": -5, "currencyCode": "pln"}
                        """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void domain_exception_is_translated_to_problem_details() throws Exception {
        when(bus.send(any(AddTransactionCommand.class))).thenThrow(new InsufficientBalanceException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST).header("Accept-Language", "pl"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:problem:payments:InsufficientBalanceException"))
                .andExpect(jsonPath("$.detail").value("Brak wystarczających środków"));
    }

    @Test
    void missing_card_is_reported_as_not_found() throws Exception {
        when(bus.send(any(AddTransactionCommand.class))).thenThrow(new CardNotFoundException());

        mockMvc.perform(post(URL).contentType(MediaType.APPLICATION_JSON).content(VALID_REQUEST))
                .andExpect(status().isNotFound());
    }

}
