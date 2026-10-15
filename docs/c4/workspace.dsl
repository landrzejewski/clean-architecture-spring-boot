/*
 Model C4 systemu płatności - Structurizr DSL.
 Podgląd: docker run -it --rm -p 8080:8080 -v $(pwd)/docs/c4:/usr/local/structurizr structurizr/lite
 */
workspace "Payments" "Przykład ze szkolenia Clean Architecture" {

    model {
        customer = person "Klient" "Posiadacz karty płatniczej"
        operator = person "Operator" "Pracownik obsługujący karty"

        payments = softwareSystem "Payments" "Wydawanie kart i rejestracja transakcji" {
            app = container "Payments Application" "Use case'y i model domeny" "Java" {
                domain = component "Domain" "Card, Money, Transaction - reguły biznesowe" "Java"
                application = component "Application" "Use case'y, porty wejściowe i wyjściowe" "Java"
                adapters = component "Adapters" "Implementacje portów (in-memory, konsola)" "Java"
            }
        }

        customer -> payments "Płaci kartą"
        operator -> payments "Wydaje karty, przegląda transakcje"
        operator -> app "Wywołuje use case'y"
        adapters -> application "Implementuje porty"
        application -> domain "Deleguje logikę"
    }

    views {
        systemContext payments "Context" {
            include *
            autolayout lr
        }
        container payments "Containers" {
            include *
            autolayout lr
        }
        component app "Components" {
            include *
            autolayout lr
        }
    }

}
