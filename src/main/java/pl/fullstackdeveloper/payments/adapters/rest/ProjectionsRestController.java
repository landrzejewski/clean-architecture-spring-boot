package pl.fullstackdeveloper.payments.adapters.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.fullstackdeveloper.payments.queries.CardProjectionRebuilder;

import java.util.Map;

// Operacja administracyjna: przebudowa modelu odczytu ze źródła prawdy
@RestController
class ProjectionsRestController {

    private final CardProjectionRebuilder rebuilder;

    ProjectionsRestController(final CardProjectionRebuilder rebuilder) {
        this.rebuilder = rebuilder;
    }

    @PostMapping("api/admin/projections/cards/rebuild")
    Map<String, Long> rebuild() {
        return Map.of("cards", rebuilder.rebuild());
    }

}
