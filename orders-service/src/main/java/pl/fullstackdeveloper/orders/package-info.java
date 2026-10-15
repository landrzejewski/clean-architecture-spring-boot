/**
 * Moduł aplikacyjny "orders". W usłudze zamówień moduł "payments" to wyłącznie kontrakt z artefaktu payments-api.
 */
@ApplicationModule(displayName = "Orders", allowedDependencies = {"payments::api", "common"})
package pl.fullstackdeveloper.orders;

import org.springframework.modulith.ApplicationModule;
