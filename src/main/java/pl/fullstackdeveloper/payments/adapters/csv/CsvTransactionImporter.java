package pl.fullstackdeveloper.payments.adapters.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.TransactionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/*
 Drugi adapter wejściowy dla tego samego portu AddTransactionUseCase (obok REST).
 Format linii: numerKarty;IN|OUT;kwota;waluta
 */
class CsvTransactionImporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvTransactionImporter.class);
    private static final String SEPARATOR = ";";

    private final AddTransactionUseCase addTransactionUseCase;

    CsvTransactionImporter(final AddTransactionUseCase addTransactionUseCase) {
        this.addTransactionUseCase = addTransactionUseCase;
    }

    ImportResult importFrom(final Reader reader) throws IOException {
        var imported = 0;
        var rejected = 0;
        try (var lines = new BufferedReader(reader)) {
            String line;
            while ((line = lines.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                try {
                    addTransactionUseCase.handle(toCommand(line));
                    imported++;
                } catch (RuntimeException exception) {
                    LOGGER.warn("Rejected line \"{}\": {}", line, exception.getMessage());
                    rejected++;
                }
            }
        }
        return new ImportResult(imported, rejected);
    }

    private AddTransactionCommand toCommand(final String line) {
        var fields = line.split(SEPARATOR);
        var type = switch (fields[1].trim()) {
            case "IN" -> TransactionType.INFLOW;
            case "OUT" -> TransactionType.PAYMENT;
            default -> throw new IllegalArgumentException("Unknown type " + fields[1]);
        };
        return new AddTransactionCommand(new CardNumber(fields[0].trim()), Money.of(fields[2].trim(), fields[3].trim()), type);
    }

    record ImportResult(int imported, int rejected) {
    }

}
