package com.OdysseyAi.demo.travel.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "google.maps")
public class GoogleMapsProperties {

    private String apiKey;

    private String placesBaseUrl =
            "https://places.googleapis.com/v1";

    private String routesBaseUrl =
            "https://routes.googleapis.com";
}