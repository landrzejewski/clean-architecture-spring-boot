package pl.fullstackdeveloper.payments.adapters.time;

import pl.fullstackdeveloper.common.annotations.Adapter;
import pl.fullstackdeveloper.payments.application.DateTimeProvider;

import java.time.ZonedDateTime;

@Adapter
final class SystemDateTimeProvider implements DateTimeProvider {

    @Override
    public ZonedDateTime getZonedDateTime() {
        return ZonedDateTime.now();
    }

}
