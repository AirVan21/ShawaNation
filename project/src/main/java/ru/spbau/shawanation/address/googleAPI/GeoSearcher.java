package ru.spbau.shawanation.address.googleAPI;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.utils.GoogleAPIKeys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GeoSearcher is a class for converting string address to formatted address via GoogleAPI
 */
public class GeoSearcher {
    private static final GeoApiContext geoContext = new GeoApiContext().setApiKey(GoogleAPIKeys.GEO_API_CODE);
    private static final String CITY = " , Санкт-Петербург";

    public static Optional<PlaceCoordinates> getLocalCityCoordinates(String location) {
        List<PlaceCoordinates> coordinates = getCityCoordinates(location + CITY);
        if (!coordinates.isEmpty()) {
            return Optional.of(coordinates.get(0));
        }
        // Try 2GIS Transport
        coordinates = ru.spbau.shawanation.address.gisAPI.GeoSearcher.getTransportCoord(location);
        if (!coordinates.isEmpty()) {
            return Optional.of(coordinates.get(0));
        }
        // Finally, try 2GIS Geocoding
        coordinates = ru.spbau.shawanation.address.gisAPI.GeoSearcher.getGeoCoord(location);

        return coordinates.isEmpty() ? Optional.empty() : Optional.of(coordinates.get(0));
    }

    public static List<PlaceCoordinates> getCityCoordinates(String location) {
        List<PlaceCoordinates> coordinates = new ArrayList<>();
        try {
            coordinates = requestPlaceCoordinates(location);
        } catch (Exception e) {
            System.out.println("Couldn't process request: " + location);
            System.out.println(e.getMessage());
        }

        return coordinates;
    }

    private static List<PlaceCoordinates> requestPlaceCoordinates(String location) throws Exception {

        return Arrays.stream(GeocodingApi
                .geocode(geoContext, location)
                .await())
                .map(PlaceCoordinates::new)
                .collect(Collectors.toList());
    }
}
