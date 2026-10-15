/*
 Model C4 systemu - Structurizr DSL (stan: 16-extract-microservice).
 Podgląd: docker run -it --rm -p 8080:8080 -v $(pwd)/docs/c4:/usr/local/structurizr structurizr/lite
 */
workspace "Shop" "Przykład ze szkolenia Clean Architecture" {

    model {
        customer = person "Klient" "Składa zamówienia, płaci kartą"
        operator = person "Operator" "Wydaje karty, przegląda transakcje"
        nbp = softwareSystem "NBP API" "Kursy walut (tabela A)" "External"

        shop = softwareSystem "Shop" "Zamówienia i płatności kartą" {
            paymentsService = container "payments-service" "Karty, transakcje, płatności" "Spring Boot 4, :8081"
            paymentsDb = container "Payments DB" "Karty, transakcje, event_publication" "PostgreSQL / H2" "Database"
            ordersService = container "orders-service" "Zamówienia" "Spring Boot 4, :8082"
            ordersDb = container "Orders DB" "Zamówienia, event_publication" "PostgreSQL / H2" "Database"
            kafka = container "Kafka" "payments.payment-requested, payments.payment-processed" "Apache Kafka" "Queue"
        }

        customer -> ordersService "Składa zamówienie" "REST"
        operator -> paymentsService "Zarządza kartami" "REST"
        ordersService -> kafka "PaymentRequested (outbox)" "Kafka"
        kafka -> paymentsService "PaymentRequested" "Kafka"
        paymentsService -> kafka "PaymentProcessed (outbox)" "Kafka"
        kafka -> ordersService "PaymentProcessed" "Kafka"
        ordersService -> paymentsService "POST /api/payments (tryb sync)" "HTTP/JSON"
        paymentsService -> paymentsDb "JPA"
        ordersService -> ordersDb "JPA"
        paymentsService -> nbp "Pobiera kursy" "HTTPS/JSON"
    }

    views {
        systemContext shop "Context" {
            include *
            autolayout lr
        }
        container shop "Containers" {
            include *
            autolayout lr
        }
        styles {
            element "External" {
                background #999999
            }
            element "Database" {
                shape cylinder
            }
            element "Queue" {
                shape pipe
            }
        }
    }

}
