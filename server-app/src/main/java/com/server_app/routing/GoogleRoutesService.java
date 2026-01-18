package com.server_app.routing;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import com.server_app.exceptions.RouteInfoException;
import com.server_app.exceptions.RouteNotFoundException;
import com.uber.common.Coordinates;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GoogleRoutesService {
    private final GeoApiContext context;

    public GoogleRoutesService(@Value("${google.api.key}") String apiKey) {
        context = new GeoApiContext.Builder()
                .apiKey(apiKey)
                .build();
    }

    public RouteInfo getRouteInfo(Coordinates coordinates, Coordinates destination) {
        try {

            LatLng origin = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
            LatLng dest = new LatLng(destination.getLatitude(), destination.getLongitude());

            log.info("Wysyłanie żądania o trasę z: {},{} do: {},{}",
                    coordinates.getLatitude(), coordinates.getLongitude(),
                    destination.getLatitude(), destination.getLongitude());

            DirectionsResult result = DirectionsApi.newRequest(context)
                    .origin(origin)
                    .destination(dest)
                    .mode(TravelMode.DRIVING)
                    .await();

            log.info("Directions API Response: {}", result.toString());

            if (result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                DirectionsLeg leg = route.legs[0];

                long durationInSeconds = leg.duration.inSeconds;
                long distanceInMeters = leg.distance.inMeters;
                String overviewPolyline = route.overviewPolyline.getEncodedPath();

                log.info("Udało się pobrać trasę: czas={} sek, dystans={} m",
                        durationInSeconds, distanceInMeters);

                return new RouteInfo(
                        (int) (durationInSeconds / 60),
                        distanceInMeters,
                        overviewPolyline
                );
            }

            // Jeśli nie znaleziono trasy, rzucamy wyjątek informujący o tym fakcie
            throw new RouteNotFoundException("No routes found for provided coordinates");
        } catch (Exception e) {
            log.error("Error fetching route from {} to {}", coordinates, destination, e);

            // Dodatkowe logowanie dla specyficznych przypadków
            if (e instanceof com.google.maps.errors.NotFoundException) {
                log.error("NotFoundException - możliwe przyczyny: nieprawidłowe współrzędne, brak dostępu do API, ograniczenia API");
            } else if (e instanceof com.google.maps.errors.ApiException) {
                log.error("ApiException: {}", e.getMessage());
            }

            // W przypadku innych błędów opakowujemy wyjątek w dedykowany wyjątek serwisowy
            throw new RouteInfoException("Error fetching route info", e);
        }
    }
}
