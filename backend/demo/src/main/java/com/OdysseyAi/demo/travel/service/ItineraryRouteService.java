package com.OdysseyAi.demo.travel.service;

import com.OdysseyAi.demo.travel.dto.HotelData;
import com.OdysseyAi.demo.travel.dto.PlaceData;
import com.OdysseyAi.demo.travel.dto.RouteData;
import com.OdysseyAi.demo.travel.interfaces.RoutesClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ItineraryRouteService {

    private final RoutesClient routesClient;

    public List<RouteData> calculateRoutes(
            List<String> locations,
            List<PlaceData> places,
            HotelData hotel
    ) {

        List<RouteData> routes =
                new ArrayList<>();

        if (locations == null
                || locations.size() < 2) {

            return routes;
        }

        /*
         * Keep only consecutive unique locations.
         *
         * Example:
         * Hotel -> A -> B -> A -> B -> Hotel
         *
         * becomes:
         * Hotel -> A
         * A -> B
         * B -> A
         * A -> B   (duplicate pair removed later)
         * B -> Hotel
         */
        List<String> cleanedLocations =
                removeConsecutiveDuplicates(
                        locations
                );

        /*
         * Prevent calling Google Routes multiple times
         * for the exact same origin -> destination pair.
         */
        Set<String> processedPairs =
                new LinkedHashSet<>();

        for (int i = 0;
             i < cleanedLocations.size() - 1;
             i++) {

            String originName =
                    cleanedLocations.get(i);

            String destinationName =
                    cleanedLocations.get(i + 1);

            if (originName == null
                    || destinationName == null
                    || originName.isBlank()
                    || destinationName.isBlank()) {

                continue;
            }

            LocationPoint origin =
                    resolveLocation(
                            originName,
                            places,
                            hotel
                    );

            LocationPoint destination =
                    resolveLocation(
                            destinationName,
                            places,
                            hotel
                    );

            /*
             * If we cannot resolve either location,
             * we cannot call Google Routes.
             */
            if (origin == null
                    || destination == null) {

                continue;
            }

            String pairKey =
                    buildPairKey(
                            origin.name(),
                            destination.name()
                    );

            if (!processedPairs.add(pairKey)) {
                continue;
            }

            /*
             * ----------------------------------------------------
             * DRIVE
             * ----------------------------------------------------
             */
            try {

                RouteData drivingRoute =
                        routesClient.calculateRoute(
                                origin.name(),
                                origin.latitude(),
                                origin.longitude(),
                                destination.name(),
                                destination.latitude(),
                                destination.longitude(),
                                "DRIVE"
                        );

                if (drivingRoute != null) {

                    routes.add(
                            drivingRoute
                    );
                }

            } catch (Exception ex) {
                // Fallback route using Haversine * 1.35 road tortuosity
                double distanceKm = calculateHaversineKm(origin.latitude(), origin.longitude(), destination.latitude(), destination.longitude());
                long distanceMeters = Math.round(distanceKm * 1000);
                long durationSeconds = Math.max(600L, Math.round(distanceKm * 130));
                routes.add(
                        RouteData.builder()
                                .origin(origin.name())
                                .destination(destination.name())
                                .travelMode("DRIVE")
                                .distanceMeters(distanceMeters)
                                .durationSeconds(durationSeconds)
                                .fare(java.math.BigDecimal.valueOf(Math.round(distanceKm * 14.0)))
                                .fareCurrency("INR")
                                .build()
                );
            }

            /*
             * ----------------------------------------------------
             * TRANSIT
             * ----------------------------------------------------
             */
            try {

                RouteData transitRoute =
                        routesClient.calculateRoute(
                                origin.name(),
                                origin.latitude(),
                                origin.longitude(),
                                destination.name(),
                                destination.latitude(),
                                destination.longitude(),
                                "TRANSIT"
                        );

                if (transitRoute != null) {

                    routes.add(
                            transitRoute
                    );
                }

            } catch (Exception ignored) {

                /*
                 * Transit may not be available for every route.
                 */
            }
        }

        return routes;
    }

    private List<String> removeConsecutiveDuplicates(
            List<String> locations
    ) {

        List<String> cleaned =
                new ArrayList<>();

        String previous = null;

        for (String location :
                locations) {

            if (location == null
                    || location.isBlank()) {

                continue;
            }

            if (previous == null
                    || !previous.equalsIgnoreCase(
                    location.trim()
            )) {

                cleaned.add(
                        location.trim()
                );

                previous =
                        location.trim();
            }
        }

        return cleaned;
    }

    private String buildPairKey(
            String origin,
            String destination
    ) {

        return normalize(origin)
                + " -> "
                + normalize(destination);
    }

    private LocationPoint resolveLocation(
            String locationName,
            List<PlaceData> places,
            HotelData hotel
    ) {

        if (locationName == null
                || locationName.isBlank()) {

            return null;
        }

        /*
         * --------------------------------------------------------
         * HOTEL
         * --------------------------------------------------------
         */
        if (hotel != null
                && hotel.getName() != null
                && hotel.getLatitude() != null
                && hotel.getLongitude() != null
                && matches(
                locationName,
                hotel.getName()
        )) {

            return new LocationPoint(
                    hotel.getName(),
                    hotel.getLatitude(),
                    hotel.getLongitude()
            );
        }

        /*
         * --------------------------------------------------------
         * SELECTED PLACES
         * --------------------------------------------------------
         */
        if (places != null) {

            for (PlaceData place :
                    places) {

                if (place == null
                        || place.getName() == null
                        || place.getLatitude() == null
                        || place.getLongitude() == null) {

                    continue;
                }

                if (matches(
                        locationName,
                        place.getName()
                )) {

                    return new LocationPoint(
                            place.getName(),
                            place.getLatitude(),
                            place.getLongitude()
                    );
                }
            }
        }

        return null;
    }

    private boolean matches(
            String first,
            String second
    ) {
        if (first == null || second == null) return false;
        String a = normalize(first);
        String b = normalize(second);

        if (a.equals(b) || a.contains(b) || b.contains(a)) {
            return true;
        }

        // Check word overlap for multi-word place names
        String[] wordsA = a.split(" ");
        for (String w : wordsA) {
            if (w.length() >= 4 && b.contains(w)) {
                return true;
            }
        }
        return false;
    }

    private double calculateHaversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double straightKm = R * c;
        return Math.max(2.5, Math.round(straightKm * 1.35 * 10.0) / 10.0);
    }

    private String normalize(
            String value
    ) {

        return value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll(
                        "\\s+",
                        " "
                );
    }

    private record LocationPoint(
            String name,
            double latitude,
            double longitude
    ) {
    }
}