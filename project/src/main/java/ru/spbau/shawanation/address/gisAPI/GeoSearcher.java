package ru.spbau.shawanation.address.gisAPI;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.exceptions.InvalidApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GeoSearcher {
    private static final String TRANSPORT_QUERY = "http://catalog.api.2gis.ru/2.0/transport/station/search";
    private static final String GEO_QUERY = "http://catalog.api.2gis.ru/2.0/geo/search";
    private static final String YANDEX_SPELLER = "http://speller.yandex.net/services/spellservice.json/checkText";
    private static final String AUTH_KEY = "ruczoy1743";
    private static final String FIELDS = "items.geometry.centroid";
    private static final int REGION_ID = 38;

    public static List<PlaceCoordinates> getTransportCoord(String location) {
        try {
            HttpResponse<JsonNode> transportResponse = Unirest.get(TRANSPORT_QUERY)
                    .queryString("key", AUTH_KEY)
                    .queryString("page", 1)
                    .queryString("region_id", REGION_ID)
                    .queryString("q", location)
                    .asJson();

            return parseTransportResponse(transportResponse.getBody().getObject());
        } catch (UnirestException | InvalidApiResponse e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public static List<PlaceCoordinates> getGeoCoord(String location) {
        HttpResponse<JsonNode> geoResponse = null;
        try {
            geoResponse = Unirest.get(GEO_QUERY)
                    .queryString("key", AUTH_KEY)
                    .queryString("page", 1)
                    .queryString("region_id", REGION_ID)
                    .queryString("fields", FIELDS)
                    .queryString("q", location)
                    .asJson();

            return parseGeoResponse(geoResponse.getBody().getObject());
        } catch (UnirestException | InvalidApiResponse e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private static List<PlaceCoordinates> parseGeoResponse(JSONObject response) throws InvalidApiResponse {
        if (response.getJSONObject("meta").getInt("code") != 200) {
            // No results is not an exceptional situation
            if (response.getJSONObject("meta").getJSONObject("error").getString("type").equals("itemNotFound")) {
                return Collections.emptyList();
            }
            throw new InvalidApiResponse(response.getJSONObject("meta").getJSONObject("error").toString(2));
        }

        JSONArray items = response.getJSONObject("result").getJSONArray("items");

        List<PlaceCoordinates> coords = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject it = items.getJSONObject(i);
            String pt = it.getJSONObject("geometry").getString("centroid");
            PlaceCoordinates placeCoordinates = placeCoordsFromPoint(pt);
            if (placeCoordinates != null) {
                coords.add(placeCoordinates);
            }
        }

        return coords;
    }

    private static List<PlaceCoordinates> parseTransportResponse(JSONObject response) throws InvalidApiResponse {
        if (response.getJSONObject("meta").getInt("code") != 200) {
            // No results is not an exceptional situation
            if (response.getJSONObject("meta").getJSONObject("error").getString("type").equals("itemNotFound")) {
                return Collections.emptyList();
            }
            throw new InvalidApiResponse(response.getJSONObject("meta").getJSONObject("error").toString(2));
        }

        JSONArray items = response.getJSONObject("result").getJSONArray("items");

        List<PlaceCoordinates> coords = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            JSONObject it = items.getJSONObject(i);
            if (it.has("links")) {
                // Parse entrance for subway station
                JSONArray entrances = it.getJSONObject("links").getJSONArray("entrances");

                if (entrances.length() == 0) {
                    continue;
                }

                JSONObject entrance = entrances.getJSONObject(0);
                JSONArray points = entrance.getJSONObject("geometry").getJSONArray("points");

                if (points.length() == 0) {
                    continue;
                }

                String pt = points.getString(0);
                PlaceCoordinates placeCoordinates = placeCoordsFromPoint(pt);

                if (placeCoordinates != null) {
                    coords.add(placeCoordinates);
                }

            } else if (it.has("platforms")) {
                // Parse platform for bus/tram station
                JSONArray platforms = it.getJSONArray("platforms");
                if (platforms.length() == 0) {
                    continue;
                }

                String pt = platforms.getJSONObject(0).getJSONObject("geometry").getString("centroid");
                PlaceCoordinates placeCoordinates = placeCoordsFromPoint(pt);

                if (placeCoordinates != null) {
                    coords.add(placeCoordinates);
                }
            }
        }

        return coords;
    }

    private static PlaceCoordinates placeCoordsFromPoint(String pt) {
        /**
         * 2GIS Point has following structure:
         *  POINT(<lng> <lat>)
         * So we take content of braces and then split it by whitespace to get two numbers.
         */
        String[] split = pt.substring(6, pt.length() - 1).split(" ");
        if (split.length != 2) {
            return null;
        }

        try {
            double lat = Double.parseDouble(split[1]);
            double lng = Double.parseDouble(split[0]);

            return new PlaceCoordinates(lat, lng);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

}
