package pl.fullstackdeveloper;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/*
 Spring Modulith wykrywa moduły (pakiety najwyższego poziomu) i weryfikuje:
 - brak cykli między modułami,
 - dostęp wyłącznie do API modułu (pakiet bazowy + @NamedInterface),
 - zgodność z allowedDependencies.
 */
class ApplicationModularityTest {

    private final ApplicationModules modules = ApplicationModules.of(PaymentsServiceApplication.class);

    @Test
    void modules_respect_boundaries() {
        modules.verify();
    }

    // Dokumentacja generowana z kodu: diagramy C4 (PlantUML) i "module canvas" w target/spring-modulith-docs
    @Test
    void writes_documentation() {
        modules.forEach(System.out::println);
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases();
    }

}
