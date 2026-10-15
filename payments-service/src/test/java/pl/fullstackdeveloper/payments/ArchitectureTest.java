package pl.fullstackdeveloper.payments;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import pl.fullstackdeveloper.common.annotations.Adapter;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.Architectures.onionArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/*
 Architektura jako kod - reguły wykonywane przy każdym buildzie (fitness functions).
 Złamanie reguły = czerwony build, a nie uwaga w code review.
 */
@AnalyzeClasses(packages = "pl.fullstackdeveloper.payments", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String DOMAIN = "..payments.domain..";
    private static final String APPLICATION = "..payments.application..";
    private static final String ADAPTERS = "..payments.adapters..";

    // --- reguła zależności (warstwy)

    @ArchTest
    static final ArchRule layers_point_inwards = layeredArchitecture().consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy(DOMAIN)
            .layer("Application").definedBy(APPLICATION)
            .layer("Adapters").definedBy(ADAPTERS)
            .layer("Configuration").definedBy("pl.fullstackdeveloper.payments")
            .whereLayer("Adapters").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Adapters", "Configuration")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Adapters", "Configuration");

    // ta sama reguła wyrażona w terminologii Onion Architecture
    @ArchTest
    static final ArchRule onion = onionArchitecture()
            .domainModels(DOMAIN)
            .applicationServices(APPLICATION)
            .adapter("rest", "..payments.adapters.rest..")
            .adapter("persistence", "..payments.adapters.persistence..")
            .adapter("exchange", "..payments.adapters.exchange..")
            .adapter("csv", "..payments.adapters.csv..")
            .adapter("cli", "..payments.adapters.cli..")
            .adapter("api", "..payments.adapters.api..")
            .adapter("events", "..payments.adapters.events..")
            .adapter("time", "..payments.adapters.time..")
            .adapter("generator", "..payments.adapters.generator..")
            .withOptionalLayers(true)
            // composition root (PaymentsConfiguration) może znać wszystkie pierścienie
            .ignoreDependency(resideInAPackage("pl.fullstackdeveloper.payments"), alwaysTrue());

    // --- niezależność rdzenia od frameworków

    @ArchTest
    static final ArchRule core_is_framework_free = noClasses().that().resideInAnyPackage(DOMAIN, APPLICATION)
            .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta.persistence..", "tools.jackson..");

    @ArchTest
    static final ArchRule domain_does_not_use_system_clock = noClasses().that().resideInAPackage(DOMAIN)
            .should().callMethod(java.time.ZonedDateTime.class, "now")
            .because("czas dostarcza port DateTimeProvider - domena musi być deterministyczna");

    // --- brak cykli między pakietami

    @ArchTest
    static final ArchRule no_cycles_between_adapters = slices().matching("..payments.adapters.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule adapters_are_independent = slices().matching("..payments.adapters.(*)..")
            .should().notDependOnEachOther()
            .because("adaptery komunikują się wyłącznie przez porty aplikacji");

    // --- konwencje nazewnicze i enkapsulacja

    @ArchTest
    static final ArchRule use_cases_are_interfaces = classes().that().resideInAPackage(APPLICATION)
            .and().haveSimpleNameEndingWith("UseCase")
            .should().beInterfaces();

    @ArchTest
    static final ArchRule services_implement_use_cases = classes().that().resideInAPackage(APPLICATION)
            .and().haveSimpleNameEndingWith("Service")
            .should().implement(com.tngtech.archunit.base.DescribedPredicate.describe("a *UseCase interface",
                    javaClass -> javaClass.getSimpleName().endsWith("UseCase")));

    @ArchTest
    static final ArchRule controllers_live_in_rest_adapter = classes().that().haveSimpleNameEndingWith("RestController")
            .should().resideInAPackage("..adapters.rest..")
            .andShould().notBePublic();

    @ArchTest
    static final ArchRule jpa_entities_are_hidden_in_adapter = classes().that().areAnnotatedWith(jakarta.persistence.Entity.class)
            .should().resideInAPackage("..adapters.persistence..")
            .andShould().notBePublic();

    @ArchTest
    static final ArchRule adapters_are_marked = classes().that().resideInAPackage(ADAPTERS)
            .and().haveSimpleNameEndingWith("Adapter")
            .should().beAnnotatedWith(Adapter.class);

}
