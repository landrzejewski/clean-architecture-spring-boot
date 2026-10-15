package pl.fullstackdeveloper.shipping;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.fullstackdeveloper.shipping.application.ScheduleShipmentService;
import pl.fullstackdeveloper.shipping.application.ScheduleShipmentUseCase;
import pl.fullstackdeveloper.shipping.application.ShipmentRepository;

@Configuration
public class ShippingConfiguration {

    @Bean
    public ScheduleShipmentUseCase scheduleShipmentUseCase(final ShipmentRepository shipmentRepository) {
        return new ScheduleShipmentService(shipmentRepository);
    }

}
