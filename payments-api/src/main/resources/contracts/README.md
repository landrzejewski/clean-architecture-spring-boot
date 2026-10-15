# Kontrakt HTTP `POST /api/payments`

Przykładowe wiadomości współdzielone przez dostawcę (`payments-service`) i konsumenta (`orders-service`):

| Plik | Znaczenie |
|---|---|
| `pay-request.json` | żądanie płatności |
| `pay-response-completed.json` | odpowiedź: płatność zrealizowana |
| `pay-response-rejected.json` | odpowiedź: płatność odrzucona |

- Dostawca weryfikuje, że jego odpowiedzi mają ten kształt (`PaymentsContractProviderTest`).
- Konsument weryfikuje, że potrafi je przetworzyć (`PaymentsContractConsumerTest`).

Zmiana tych plików to zmiana kontraktu, a więc zmiana wersji `payments-api`.
W większych systemach tę rolę pełnią narzędzia CDC: Spring Cloud Contract lub Pact.
