package ru.spbau.shawanation.correction;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.spbau.shawanation.exceptions.InvalidApiResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Corrector {
    private static final String TRANSPORT_QUERY = "http://catalog.api.2gis.ru/2.0/transport/station/search";
    private static final String GEO_QUERY = "http://catalog.api.2gis.ru/2.0/geo/search";
    private static final String YANDEX_SPELLER = "http://speller.yandex.net/services/spellservice.json/checkText";
    private static final String AUTH_KEY = "ruczoy1743";
    private static final String FIELDS = "items.geometry.centroid";
    private static final int REGION_ID = 38;

    private static final WordDistance distance = new DamerauLevenshteinAlgorithm(
            1, 1, 2, 1
    );
    /**
     * Returns:
     *  - passed 'query', if it doesn't need any corrections or there is some
     *    software error in spell checking (like, exception was thrown)
     *  - null, if 'query' is definitely needs some corrections, but Corrector
     *    couldn't find one
     *  - otherwise, best correction is returned
     */
    public String getCorrection(String query) {
        try {
            // Get 2GIS suggestions
            HttpResponse<JsonNode> transportResponse = Unirest.get(TRANSPORT_QUERY)
                    .queryString("key", AUTH_KEY)
                    .queryString("page", 1)
                    .queryString("region_id", REGION_ID)
                    .queryString("q", query)
                    .asJson();
            List<String> transportCorrect = parse2GisCorrection(transportResponse);

            HttpResponse<JsonNode> geoResponse = Unirest.get(GEO_QUERY)
                    .queryString("key", AUTH_KEY)
                    .queryString("page", 1)
                    .queryString("region_id", REGION_ID)
                    .queryString("fields", FIELDS)
                    .queryString("q", query)
                    .asJson();
            List<String> geoCorrect = parse2GisCorrection(geoResponse);

            // Get Yandex Suggestions
            HttpResponse<JsonNode> yandexResponse = Unirest.get(YANDEX_SPELLER)
                    .queryString("text", query)
                    .asJson();
            List<String> yandexCorrect = parseYandexCorrection(yandexResponse);
            // If Yandex have returned NULL then query doesn't need any corrections
            if (yandexCorrect == null) {
                return query;
            }

            // Otherwise, merge corrections and choose best one
            List<String> allCorrections = new ArrayList<>(
                    geoCorrect.size() + transportCorrect.size() + yandexCorrect.size());
            allCorrections.addAll(geoCorrect);
            allCorrections.addAll(transportCorrect);
            allCorrections.addAll(yandexCorrect);

            return findBestCorrection(query, allCorrections);

        } catch (UnirestException e) {
            e.printStackTrace();
            return query;
        } catch (InvalidApiResponse invalidApiResponse) {
            invalidApiResponse.printStackTrace();
            return query;
        }
    }

    /**
     * Returns NULL if query doesn't need any corrections.
     *
     * Empty list is returned when service can't find any corrections
     */
    private List<String> parseYandexCorrection(HttpResponse<JsonNode> yandexResponse) {
        // Empty array indicates that everything is OK
        JSONArray top = yandexResponse.getBody().getArray();
        if (top.length() == 0) {
            return null;
        }

        JSONObject response = top.getJSONObject(0);
        
        List<String> result = new ArrayList<>();
        JSONArray suggestions = response.getJSONArray("s");
        for (int i = 0; i < suggestions.length(); i++) {
            result.add(suggestions.getString(i));
        }

        return result;
    }

    private String findBestCorrection(String query, List<String> corrections) {
        // First of all, check whether any correction is needed at all, i.e. check if
        // there is suggestion that is equal to query.
        if (corrections.stream().anyMatch(query::equals)) {
            return query;
        }

        // Then, check if there is some correction to choose from. Otherwise, return
        // null, indicating that query is totally unrecognizable.
        if (corrections.isEmpty()) {
            return null;
        }

        // Finally, find best correction according to the chosen WordDistance
        double[] distances = new double[corrections.size()];
        for (int i = 0; i < corrections.size(); i++) {
            String c = corrections.get(i);
            distances[i] = distance.getDistance(query, c);
        }

        double bestDist = 1e9;
        int indOfBest = -1;
        for (int i = 0; i < distances.length; i++) {
            if (distances[i] < bestDist) {
                bestDist = distances[i];
                indOfBest = i;
            }
        }

        return corrections.get(indOfBest);
    }

    private List<String> parse2GisCorrection(HttpResponse<JsonNode> geoResponse) throws InvalidApiResponse {
        JSONObject body = geoResponse.getBody().getObject();
        if (body.getJSONObject("meta").getInt("code") != 200) {
            // No results is not an exceptional situation
            if (body.getJSONObject("meta").getJSONObject("error").getString("type").equals("itemNotFound")) {
                return Collections.emptyList();
            }
            throw new InvalidApiResponse(body.getJSONObject("meta").getJSONObject("error").toString(2));
        }

        JSONArray items = body.getJSONObject("result").getJSONArray("items");

        List<String> corrections = new ArrayList<>();

        for (int i = 0; i < items.length(); i++) {
            corrections.add(items.getJSONObject(i).optString("name", null));
        }

        return corrections.stream().filter(it -> it != null).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        String query = "вкуснысоус";
        Corrector corrector = new Corrector();
        String correction = corrector.getCorrection(query);
        System.out.println(correction);
    }
}
