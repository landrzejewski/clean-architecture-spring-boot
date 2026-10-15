package pl.fullstackdeveloper.payments.adapters;

import pl.fullstackdeveloper.payments.application.DateTimeProvider;

import java.time.ZonedDateTime;

public final class SystemDateTimeProvider implements DateTimeProvider {

    @Override
    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now();
    }

}
