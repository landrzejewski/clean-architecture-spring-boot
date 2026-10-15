package pl.fullstackdeveloper.payments.adapters.csv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import pl.fullstackdeveloper.payments.application.AddTransactionUseCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

// Co kilka sekund sprawdza katalog i importuje pliki *.csv (włączane property payments.import.enabled=true)
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "payments.import.enabled", havingValue = "true")
class CsvImportScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvImportScheduler.class);

    private final CsvTransactionImporter importer;
    private final Path directory;

    CsvImportScheduler(final AddTransactionUseCase addTransactionUseCase,
                       @Value("${payments.import.directory:import}") final Path directory) {
        this.importer = new CsvTransactionImporter(addTransactionUseCase);
        this.directory = directory;
    }

    @Scheduled(fixedDelayString = "${payments.import.interval:5s}")
    void importFiles() throws IOException {
        Files.createDirectories(directory.resolve("processed"));
        try (var files = Files.newDirectoryStream(directory, "*.csv")) {
            for (var file : files) {
                try (var reader = Files.newBufferedReader(file)) {
                    LOGGER.info("Imported {}: {}", file.getFileName(), importer.importFrom(reader));
                }
                Files.move(file, directory.resolve("processed").resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

}
