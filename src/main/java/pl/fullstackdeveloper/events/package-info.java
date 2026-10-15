/**
 * Wspólny język zdarzeń procesu zamówienia (published language). W choreografii każdy moduł reaguje
 * na zdarzenia innych - gdyby typy zdarzeń leżały w modułach-nadawcach, powstałyby cykle
 * (orders -> payments -> shipping -> orders). Wydzielony moduł zdarzeń przerywa te cykle.
 * W systemie rozproszonym tę rolę pełni rejestr schematów (np. Avro/JSON Schema dla topiców).
 */
@ApplicationModule(displayName = "Shop events", type = ApplicationModule.Type.OPEN)
package pl.fullstackdeveloper.events;

import org.springframework.modulith.ApplicationModule;
