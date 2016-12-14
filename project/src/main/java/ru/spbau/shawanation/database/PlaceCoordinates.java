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

    public PlaceCoordinates(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public PlaceCoordinates(GeocodingResult location) {
        lat = location.geometry.location.lat;
        lng = location.geometry.location.lng;
        country = getParameterFromGeocoding(location, COUNTRY_ID);
        city = getParameterFromGeocoding(location, CITY_ID);
        formattedAddress = location.formattedAddress;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Formatted address: ").append(formattedAddress).append("\n");
        sb.append("Country: ").append(country).append("\n");
        sb.append("City: ").append(city).append("\n");
        sb.append("Lat: ").append(lat).append("\n");
        sb.append("Lon: ").append(lng).append("\n");

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
     * Calculate distance (in meters) between two points in latitude and longitude taking
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaceCoordinates that = (PlaceCoordinates) o;

        if (Double.compare(that.lat, lat) != 0) return false;
        return Double.compare(that.lng, lng) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(lat);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lng);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
