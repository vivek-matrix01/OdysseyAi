package com.OdysseyAi.demo.ai.client;

import com.OdysseyAi.demo.ai.configuration.GroqProperties;
import com.OdysseyAi.demo.ai.dto.response.GeneratedItineraryResponse;
import com.OdysseyAi.demo.ai.dto.response.TripRecommendationResponse;
import com.OdysseyAi.demo.ai.interfaces.AiClient;
import com.OdysseyAi.demo.common.exception.customexception.AiGenerationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Component
@RequiredArgsConstructor
public class GroqAiClient implements AiClient {

    private final GroqProperties groqProperties;
    private final ObjectMapper objectMapper;

    private final RestClient restClient =
            RestClient.builder().build();

    @Override
    public GeneratedItineraryResponse generateItinerary(
            String prompt
    ) {

        String content =
                callGroq(
                        prompt,
                        """
                        You are a travel itinerary generation API.

                        Return ONLY valid JSON.

                        The JSON must contain:
                        {
                          "days": [
                            {
                              "dayNumber": 1,
                              "date": "YYYY-MM-DD",
                              "notes": "string or null",
                              "activities": [
                                {
                                  "title": "string",
                                  "description": "string or null",
                                  "startTime": "HH:mm:ss or null",
                                  "endTime": "HH:mm:ss or null",
                                  "location": "string or null",
                                  "estimatedCost": 0.0,
                                  "currency": "string or null",
                                  "type": "SIGHTSEEING"
                                }
                              ]
                            }
                          ]
                        }

                        Do not return markdown.
                        Do not return explanations.
                        """
                );

        try {

            return objectMapper.readValue(
                    content,
                    GeneratedItineraryResponse.class
            );

        } catch (Exception ex) {

            throw new AiGenerationException(
                    "Failed to parse Groq itinerary response.",
                    ex
            );
        }
    }

    @Override
    public TripRecommendationResponse recommendTrip(
            String prompt
    ) {

        String content =
                callGroq(
                        prompt,
                        """
                        You are OdysseyAI's travel recommendation API.
    
                        Return ONLY valid JSON.
    
                        The JSON must contain exactly:
    
                        {
                          "destination": "string",
                          "tripType": "string",
                          "budget": 0.0,
                          "currency": "INR",
                          "travelers": 1,
                          "estimatedTotalCost": 0.0,
                          "places": [
                            {
                              "placeId": "string",
                              "name": "string",
                              "address": "string",
                              "category": "string",
                              "rating": 0.0,
                              "userRatingCount": 0,
                              "priceLevel": "string",
                              "estimatedCost": 0.0,
                              "reason": "string"
                            }
                          ],
                          "hotels": [
                            {
                              "propertyToken": "string",
                              "name": "string",
                              "address": "string",
                              "rating": 0.0,
                              "priceLevel": "string",
                              "estimatedNightlyCost": 0.0,
                              "reason": "string"
                            }
                          ],
                          "recommendationReason": "string"
                        }
    
                        Rules:
    
                        - Recommend places only from supplied places.
                        - placeId must be copied exactly from supplied data.
                        - Never invent a placeId.
                        - Never modify a placeId.
                        - Recommend hotels only from supplied hotel data.
                        - propertyToken must be copied exactly.
                        - Never invent or modify a propertyToken.
                        - Never invent prices.
                        - If hotel data is unavailable, return an empty hotels array.
                        - Return valid JSON only.
                        - Do not return markdown.
                        """
                );

        try {

            return objectMapper.readValue(
                    content,
                    TripRecommendationResponse.class
            );

        } catch (Exception ex) {

            throw new AiGenerationException(
                    "Failed to parse Groq recommendation response.",
                    ex
            );
        }
    }

    private String callGroq(
            String prompt,
            String systemContent
    ) {

        ObjectNode requestBody =
                objectMapper.createObjectNode();

        requestBody.put(
                "model",
                groqProperties.getModel()
        );

        requestBody.put(
                "temperature",
                0.2
        );

        requestBody.put(
                "max_completion_tokens",
                4000
        );

        requestBody.put(
                "reasoning_effort",
                "low"
        );

        ArrayNode messages =
                requestBody.putArray(
                        "messages"
                );

        ObjectNode systemMessage =
                messages.addObject();

        systemMessage.put(
                "role",
                "system"
        );

        systemMessage.put(
                "content",
                systemContent
        );

        ObjectNode userMessage =
                messages.addObject();

        userMessage.put(
                "role",
                "user"
        );

        userMessage.put(
                "content",
                prompt
        );

        ObjectNode responseFormat =
                requestBody.putObject(
                        "response_format"
                );

        responseFormat.put(
                "type",
                "json_object"
        );

        try {

            String responseBody =
                    restClient.post()
                            .uri(
                                    groqProperties.getBaseUrl()
                                            + "/chat/completions"
                            )
                            .header(
                                    "Authorization",
                                    "Bearer "
                                            + groqProperties.getApiKey()
                            )
                            .contentType(
                                    MediaType.APPLICATION_JSON
                            )
                            .body(requestBody)
                            .retrieve()
                            .body(String.class);

            JsonNode content =
                    objectMapper.readTree(
                                    responseBody
                            )
                            .path("choices")
                            .path(0)
                            .path("message")
                            .path("content");

            if (content.isMissingNode()
                    || content.isNull()
                    || content.asText().isBlank()) {

                throw new AiGenerationException(
                        "Groq returned an empty response."
                );
            }

            return content.asText();

        } catch (RestClientException ex) {

            throw new AiGenerationException(
                    "Groq service is currently unavailable.",
                    ex
            );

        } catch (AiGenerationException ex) {

            throw ex;

        } catch (Exception ex) {

            throw new AiGenerationException(
                    "Failed to process Groq response.",
                    ex
            );
        }
    }
}