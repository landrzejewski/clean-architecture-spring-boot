package pl.fullstackdeveloper;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

/*
 Spring Modulith wykrywa moduły (pakiety najwyższego poziomu) i weryfikuje:
 - brak cykli między modułami,
 - dostęp wyłącznie do API modułu (pakiet bazowy + @NamedInterface),
 - zgodność z allowedDependencies.
 */
class ApplicationModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(OrdersServiceApplication.class);

    @Test
    void modules_respect_boundaries() {
        modules.verify();
    }

}
