package com.OdysseyAi.demo.travel.interfaces;

import com.OdysseyAi.demo.travel.dto.HotelData;

import java.time.LocalDate;
import java.util.List;

public interface HotelClient {

    List<HotelData> searchHotels(
            String destination,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int adults,
            String currency
    );
}