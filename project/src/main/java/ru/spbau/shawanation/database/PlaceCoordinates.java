package ru.spbau.shawanation.database;

import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;


/**
 * PlaceCoordinates class for storing coordinates of a place
 */
public class PlaceCoordinates {
    private final static String COUNTRY_ID = "COUNTRY";
    private final static String CITY_ID    = "ADMINISTRATIVE_AREA_LEVEL_2";
    private String formattedAddress        = "";
    private String country                 = "";
    private String city                    = "";
    private double lat;
    private double lng;

    public PlaceCoordinates() {}

    public PlaceCoordinates(GeocodingResult location) {
        lat = location.geometry.location.lat;
        lng = location.geometry.location.lng;
        country = getParameterFromGeocoding(location, COUNTRY_ID);
        city = getParameterFromGeocoding(location, CITY_ID);
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

        sb.append("Lon: ");
        sb.append(lng);
        sb.append("\n");

        return sb.toString();
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference.
     */
    public Double getDistance(double inputLat, double inputLng) {
        // Radius of the earth
        final int R = 6371;
        final Double latDistance = Math.toRadians(lat - inputLat);
        final Double lonDistance = Math.toRadians(lng - inputLng);
        final Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(inputLat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        final Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000;
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
