package pl.fullstackdeveloper.compare.clean.interfaceadapters.controllers;

import pl.fullstackdeveloper.compare.clean.usecases.TopUpInputBoundary;
import pl.fullstackdeveloper.compare.clean.usecases.TopUpRequest;

import java.math.BigDecimal;

public class TopUpController {

    private final TopUpInputBoundary inputBoundary;

    public TopUpController(TopUpInputBoundary inputBoundary) {
        this.inputBoundary = inputBoundary;
    }

    public void topUp(String cardNumber, String amount) {
        inputBoundary.topUp(new TopUpRequest(cardNumber, new BigDecimal(amount)));
    }

}
