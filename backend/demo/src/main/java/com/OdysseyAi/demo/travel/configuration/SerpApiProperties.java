package com.OdysseyAi.demo.travel.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "serpapi")
public class SerpApiProperties {

    private String apiKey;

    private String baseUrl = "https://serpapi.com/search.json";
}