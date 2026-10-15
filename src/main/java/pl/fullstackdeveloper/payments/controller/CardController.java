package pl.fullstackdeveloper.payments.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.fullstackdeveloper.payments.model.CardEntity;
import pl.fullstackdeveloper.payments.service.CardService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/*
 Kontroler zwraca encje JPA bezpośrednio - zmiana schematu bazy zmienia kontrakt REST
 (np. wyciek pola "id" i pełnej listy transakcji).
 */
@RestController
@RequestMapping("api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public CardEntity addCard(@RequestBody Map<String, String> request) {
        return cardService.addCard(request.get("currencyCode"));
    }

    @PostMapping("{number}/transactions")
    public CardEntity addTransaction(@PathVariable String number, @RequestBody Map<String, String> request) {
        return cardService.addTransaction(number, new BigDecimal(request.get("amount")), request.get("currencyCode"), request.get("type"));
    }

    @GetMapping
    public List<CardEntity> getCards() {
        return cardService.getCards();
    }

    @GetMapping("{number}")
    public CardEntity getCard(@PathVariable String number) {
        return cardService.getCard(number);
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<String> onException(RuntimeException exception) {
        return ResponseEntity.badRequest().body(exception.getMessage());
    }

}
