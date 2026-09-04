package com.OdysseyAi.demo.travel.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.OdysseyAi.demo.travel.configuration.SerpApiProperties;
import com.OdysseyAi.demo.travel.interfaces.PlacePhotoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
@Primary
@RequiredArgsConstructor
public class SerpApiPlacePhotoClient implements PlacePhotoClient {

    private final SerpApiProperties properties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.builder().build();

    @Override
    public List<String> getPhotoUrls(String query) {
        List<String> imageUrls = new ArrayList<>();
        
        if (query == null || query.isBlank()) {
            return imageUrls;
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                    .queryParam("engine", "google_local")
                    .queryParam("q", query)
                    .queryParam("api_key", properties.getApiKey())
                    .build()
                    .toUriString();

            String response = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode localResults = rootNode.path("local_results");

            if (localResults.isArray() && localResults.size() > 0) {
                JsonNode firstResult = localResults.get(0);
                
                String thumbnailLarge = firstResult.path("thumbnail_large").asText(null);
                if (thumbnailLarge != null && !thumbnailLarge.isBlank()) {
                    imageUrls.add(thumbnailLarge);
                } else {
                    String thumbnail = firstResult.path("thumbnail").asText(null);
                    if (thumbnail != null && !thumbnail.isBlank()) {
                        imageUrls.add(thumbnail);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors to not break the flow, just return empty list
        }

        return imageUrls;
    }
}
