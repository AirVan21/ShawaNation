package ru.spbau.database;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;

import java.util.Optional;

/**
 * Created by airvan21 on 24.09.16.
 */
public class PlaceCoordinates {
    private final static String countryId  = "COUNTRY";
    private String country  = "";
    private double lat;
    private double lng;

    public PlaceCoordinates() {}

    public PlaceCoordinates(GeocodingResult location) {
        lat = location.geometry.location.lat;
        lng = location.geometry.location.lng;
        country  = getParameterFromGeocoding(location, countryId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Country: ");
        sb.append(country);
        sb.append("\n");

        sb.append("Lat: ");
        sb.append(lat);
        sb.append("\n");

        sb.append("Lan: ");
        sb.append(lng);
        sb.append("\n");

        return sb.toString();
    }

    private String getParameterFromGeocoding(GeocodingResult location, String parameter) {
        for (AddressComponent item : location.addressComponents) {
            if (item.types.length > 0 && item.types[0].name().equals(parameter)) {
                return item.longName;
            }
        }

        return "";
    }
}
