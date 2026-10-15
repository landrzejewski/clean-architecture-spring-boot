package pl.fullstackdeveloper.payments;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// Reguła zależności dla właściwej aplikacji płatności (styl heksagonalny z 02)
class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("pl.fullstackdeveloper.payments");

    @Test
    void domain_depends_only_on_itself() {
        noClasses().that().resideInAPackage("..payments.domain..")
                .should().dependOnClassesThat().resideInAnyPackage("..payments.application..", "..payments.adapters..")
                .check(classes);
    }

    @Test
    void domain_and_application_do_not_depend_on_frameworks() {
        noClasses().that().resideInAnyPackage("..payments.domain..", "..payments.application..")
                .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..")
                .check(classes);
    }

    @Test
    void application_does_not_depend_on_adapters() {
        noClasses().that().resideInAPackage("..payments.application..")
                .should().dependOnClassesThat().resideInAPackage("..payments.adapters..")
                .check(classes);
    }

}
