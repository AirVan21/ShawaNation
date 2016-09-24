package ru.spbau.database;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;


/**
 * Class for storing
 */
public class PlaceCoordinates {
    private final static String countryId = "COUNTRY";
    private final static String cityId    = "ADMINISTRATIVE_AREA_LEVEL_2";
    private String formattedAddress       = "";
    private String country                = "";
    private String city                   = "";
    private double lat;
    private double lng;

    public PlaceCoordinates() {}

    public PlaceCoordinates(GeocodingResult location) {
        lat = location.geometry.location.lat;
        lng = location.geometry.location.lng;
        country = getParameterFromGeocoding(location, countryId);
        city    = getParameterFromGeocoding(location, cityId);
        formattedAddress = location.formattedAddress;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Formatted address: ");
        sb.append(formattedAddress);
        sb.append("\n");

        sb.append("Country: ");
        sb.append(country);
        sb.append("\n");

        sb.append("City: ");
        sb.append(city);
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
