/*
 Model C4 systemu - Structurizr DSL (stan: 10-second-context-naive).
 Podgląd: docker run -it --rm -p 8080:8080 -v $(pwd)/docs/c4:/usr/local/structurizr structurizr/lite
 */
workspace "Shop" "Przykład ze szkolenia Clean Architecture" {

    model {
        customer = person "Klient" "Składa zamówienia, płaci kartą"
        operator = person "Operator" "Wydaje karty, przegląda transakcje"
        nbp = softwareSystem "NBP API" "Kursy walut (tabela A)" "External"

        shop = softwareSystem "Shop" "Zamówienia i płatności kartą" {
            app = container "Shop Application" "Monolit Spring Boot" "Java 25, Spring Boot 4" {
                orders = component "Orders" "Składanie zamówień" "orders.*"
                payments = component "Payments" "Karty, transakcje, przeliczanie walut" "payments.*"
            }
            db = container "Database" "Karty i transakcje" "PostgreSQL / H2" "Database"
        }

        customer -> orders "Składa zamówienie" "REST"
        operator -> payments "Zarządza kartami" "REST"
        orders -> payments "Obciąża kartę (bezpośrednio przez CardRepository i Card!)"
        payments -> orders "Pokazuje zamówienia karty (cykl!)"
        payments -> db "Czyta i zapisuje" "JPA / JDBC"
        payments -> nbp "Pobiera kursy" "HTTPS/JSON"
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
        component app "Components" {
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
        }
    }

}
