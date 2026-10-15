/**
 * Moduł aplikacyjny "orders". W choreografii zna wyłącznie wspólny język zdarzeń (moduł events).
 */
@ApplicationModule(displayName = "Orders", allowedDependencies = {"events", "common"})
package pl.fullstackdeveloper.orders;

import org.springframework.modulith.ApplicationModule;
