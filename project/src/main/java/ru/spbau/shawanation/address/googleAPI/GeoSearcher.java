package ru.spbau.shawanation.address.googleAPI;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import ru.spbau.shawanation.database.PlaceCoordinates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * GeoSearcher is a class for converting string address to formatted address via GoogleAPI
 */
public class GeoSearcher {
    private static final String GEO_API_CODE = "AIzaSyBPGuEnVZcQarLwzByVquiP4D-lmc2Q9OY";
    private static final GeoApiContext geoContext = new GeoApiContext().setApiKey(GEO_API_CODE);

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
        List<PlaceCoordinates> geoInformation = Arrays.stream(GeocodingApi
                .geocode(geoContext, location)
                .await())
                .map(PlaceCoordinates::new)
                .collect(Collectors.toList());

        return geoInformation;
    }
}
