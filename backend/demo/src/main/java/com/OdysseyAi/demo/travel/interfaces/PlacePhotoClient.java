package com.OdysseyAi.demo.travel.interfaces;

import java.util.List;

public interface PlacePhotoClient {

    List<String> getPhotoUrls(
            String query
    );
}