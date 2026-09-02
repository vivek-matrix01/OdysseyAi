package com.OdysseyAi.demo.travel.interfaces;

public interface PlacePhotoClient {

    String getPhotoUrl(
            String photoName,
            int maxWidthPx,
            int maxHeightPx
    );
}