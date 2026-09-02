package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.trip.entity.Activity;
import com.OdysseyAi.demo.trip.entity.Itinerary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TripCostService {

    public CostBreakdown calculate(
            Itinerary itinerary,
            HotelData selectedHotel,
            BigDecimal transportCost
    ) {

        BigDecimal hotelCost =
                calculateHotelCost(
                        selectedHotel
                );

        BigDecimal activityCost =
                calculateActivityCost(
                        itinerary
                );

        BigDecimal safeTransportCost =
                transportCost == null
                        ? BigDecimal.ZERO
                        : transportCost;

        BigDecimal totalCost =
                hotelCost
                        .add(activityCost)
                        .add(safeTransportCost)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        return new CostBreakdown(
                hotelCost.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                activityCost.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                safeTransportCost.setScale(
                        2,
                        RoundingMode.HALF_UP
                ),
                totalCost
        );
    }

    private BigDecimal calculateHotelCost(
            HotelData hotel
    ) {

        if (hotel == null
                || hotel.getTotalPrice() == null) {

            return BigDecimal.ZERO;
        }

        return hotel.getTotalPrice();
    }

    private BigDecimal calculateActivityCost(
            Itinerary itinerary
    ) {

        if (itinerary == null
                || itinerary.getDays() == null) {

            return BigDecimal.ZERO;
        }

        BigDecimal total =
                BigDecimal.ZERO;

        for (var day :
                itinerary.getDays()) {

            if (day == null
                    || day.getActivities() == null) {

                continue;
            }

            for (Activity activity :
                    day.getActivities()) {

                if (activity == null
                        || activity.getEstimatedCost() == null) {

                    continue;
                }

                total =
                        total.add(
                                activity.getEstimatedCost()
                        );
            }
        }

        return total;
    }

    public record CostBreakdown(
            BigDecimal hotelCost,
            BigDecimal activityCost,
            BigDecimal transportCost,
            BigDecimal totalCost
    ) {
    }
}