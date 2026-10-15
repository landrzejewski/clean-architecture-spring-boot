/**
 * Moduł aplikacyjny "orders". Orkiestruje sagę składania zamówienia; zna tylko API płatności i wysyłek.
 */
@ApplicationModule(displayName = "Orders", allowedDependencies = {"payments::api", "shipping::api", "common"})
package pl.fullstackdeveloper.orders;

import org.springframework.modulith.ApplicationModule;
