package pl.fullstackdeveloper.payments;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
 Moduł płatności testujemy w izolacji - bez zamówień i bez modułu app.
 Skanujemy pl.fullstackdeveloper, żeby objąć także aspekty z modułu common.
 */
@SpringBootApplication(scanBasePackages = "pl.fullstackdeveloper")
class PaymentsTestApplication {
}
