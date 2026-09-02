package com.OdysseyAi.demo.travel.client;

import com.OdysseyAi.demo.travel.configuration.GoogleMapsProperties;
import com.OdysseyAi.demo.travel.interfaces.PlacePhotoClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GooglePlacePhotoClient
        implements PlacePhotoClient {

    private final GoogleMapsProperties properties;

    @Override
    public String getPhotoUrl(
            String photoName,
            int maxWidthPx,
            int maxHeightPx
    ) {

        if (photoName == null
                || photoName.isBlank()) {

            return null;
        }

        return properties.getPlacesBaseUrl()
                + "/"
                + photoName
                + "/media"
                + "?maxWidthPx="
                + maxWidthPx
                + "&maxHeightPx="
                + maxHeightPx
                + "&key="
                + properties.getApiKey();
    }
}