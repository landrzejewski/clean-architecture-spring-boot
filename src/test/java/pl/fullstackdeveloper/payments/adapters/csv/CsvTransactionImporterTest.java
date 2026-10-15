package pl.fullstackdeveloper.payments.adapters.csv;

import org.junit.jupiter.api.Test;
import pl.fullstackdeveloper.payments.application.AddTransactionCommand;
import pl.fullstackdeveloper.payments.domain.CardNumber;
import pl.fullstackdeveloper.payments.domain.Money;
import pl.fullstackdeveloper.payments.domain.TransactionId;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.fullstackdeveloper.payments.domain.TransactionType.INFLOW;
import static pl.fullstackdeveloper.payments.domain.TransactionType.PAYMENT;

class CsvTransactionImporterTest {

    private final List<AddTransactionCommand> commands = new ArrayList<>();
    private final CsvTransactionImporter importer = new CsvTransactionImporter(command -> {
        commands.add(command);
        return new TransactionId();
    });

    @Test
    void valid_lines_are_imported_and_invalid_rejected() throws Exception {
        var csv = """
                1111222233334444;IN;100;PLN
                1111222233334444;OUT;25.50;PLN
                bad-card;IN;1;PLN
                """;

        var result = importer.importFrom(new StringReader(csv));

        assertThat(result).isEqualTo(new CsvTransactionImporter.ImportResult(2, 1));
        var card = new CardNumber("1111222233334444");
        assertThat(commands).containsExactly(
                new AddTransactionCommand(card, Money.of("100", "PLN"), INFLOW),
                new AddTransactionCommand(card, Money.of("25.50", "PLN"), PAYMENT));
    }

}
