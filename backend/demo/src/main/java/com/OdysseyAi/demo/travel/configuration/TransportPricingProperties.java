package com.OdysseyAi.demo.travel.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "transport.pricing")
public class TransportPricingProperties {

    /*
     * Application-level assumption for driving.
     *
     * This is NOT supplied by Google.
     */
    private BigDecimal drivingCostPerKm =
            BigDecimal.valueOf(15);
}