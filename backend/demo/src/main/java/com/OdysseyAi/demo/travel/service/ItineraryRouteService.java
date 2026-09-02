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

            } catch (Exception ignored) {

                /*
                 * Do not fail the entire itinerary if
                 * driving route is unavailable.
                 */
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

        String a =
                normalize(first);

        String b =
                normalize(second);

        return a.equals(b)
                || a.contains(b)
                || b.contains(a);
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