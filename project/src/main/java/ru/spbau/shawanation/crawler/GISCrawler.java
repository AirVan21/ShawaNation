package ru.spbau.shawanation.crawler;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GISCrawler implements Crawler {
    /** Some constants for making search request into 2GIS Organizations API **/
    private static final String ORGANIZATIONS_API = "http://catalog.api.2gis.ru";
    private static final String API_VERSION = "2.0";
    // have no idea wtf is this, but it is required
    private static final String UNKNOWN_MAGIC = "catalog/branch";
    private static final String SEARCH_METHOD = "search";
    // totally private!
    private static final String AUTH_KEY = "ruczoy1743";
    private static final String SEARCH_QUERY = ORGANIZATIONS_API + "/" + API_VERSION + "/" + UNKNOWN_MAGIC + "/" + SEARCH_METHOD;
    // long string literal with enumeration of all fields that should be sent in response;
    private static final String ADDITIONAL_FIELDS = "items.contact_groups,items.address,items.name_ex,items.point,items.org,items.schedule,items.reviews";

    /** Some constant or making search request in 2GIS Reviews API **/
    private static final String REVIEWS_API = "http://api.reviews.2gis.com";
    private static final String REVIEWS_API_VERSION = "1.0";
    private static final String REVIEWS_METHOD = "reviews/get";
    private static final String OBJECT_TYPE = "branch"; // again, no idea wtf is this
    private static final String REVIEWS_QUERY = REVIEWS_API + "/" + REVIEWS_API_VERSION + "/" + REVIEWS_METHOD;


    private final List<Post> posts = new ArrayList<>();

    private void processSearchResponse(HttpResponse<JsonNode> httpResponse) {
        JSONArray items = httpResponse
                .getBody()
                .getObject()
                .getJSONObject("result")
                .getJSONArray("items");

        for (int i = 0; i < items.length(); ++i) {
            processSearchItem(items.getJSONObject(i));
        }
    }

    private void processSearchItem(JSONObject item) {
        String idForReviewsAPI = getIDInReviewsAPI(item);
        PlaceCoordinates coords = new PlaceCoordinates(
                getLatitude(item),
                getLongitude(item)
        );

        // now get all reviews from reviews API
        HttpResponse<JsonNode> reviewsHttpResponse;
        try {
            reviewsHttpResponse = Unirest.get(REVIEWS_QUERY)
                    .queryString("object_type", OBJECT_TYPE)
                    .queryString("object_id", idForReviewsAPI)
                    .queryString("page_size", 20)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return;
        }

        collectReviews(coords, reviewsHttpResponse.getBody().getObject());
    }

    private void collectReviews(PlaceCoordinates coords, JSONObject curResponse) {
        // 2GIS sends link to the next page of reviews in the response itself. How convenient!
        String nextLink = curResponse.getJSONObject("result").getString("next_link").replace("https", "http");

        // Iterate over reviews in the current page
        JSONArray reviews = curResponse.getJSONObject("result").getJSONArray("reviews");
        for (int i = 0; i < reviews.length(); i++) {
            JSONObject curReview = reviews.getJSONObject(i);
            String reviewSource = curReview.getString("source");

            // Get only Flamp reviews
            // (Foursquare reviews are parsed from native Foursquare API which offers more complete selection)
            if (!reviewSource.equals("flamp")) {
                continue;
            }
            int rating = curReview.getInt("rating");

            String text = curReview.getString("text");

            // Finally, place a new Post in the buffer
            // Note that we multiply rating by 2 to convert from 5-max to the 10-max scale.
            posts.add(new Post(text, (double) rating * 2, coords));
        }

        // get next response
        if (!nextLink.isEmpty()) {
            JSONObject nextResponse = null;
            try {
                nextResponse = Unirest.get(nextLink).asJson().getBody().getObject();
            } catch (UnirestException e) {
                e.printStackTrace();
                return;
            }
            collectReviews(coords, nextResponse);
        }
    }

    private double getLongitude(JSONObject item) {
        return item.getJSONObject("point").getDouble("lon");
    }

    private double getLatitude(JSONObject item) {
        return item.getJSONObject("point").getDouble("lat");
    }

    private String getIDInReviewsAPI(JSONObject item) {
        String fullID = item.getString("id");
        // take substring till first underscore (this corresponds to id, that is used for reviews api)
        int underscoreIndex = fullID.indexOf('_');
        return fullID.substring(0, underscoreIndex);
    }

    @Override
    public List<Post> getPosts() {
        HttpResponse<JsonNode> httpResponse = null;
        /* Make first request to get amount of results */
        try {
            httpResponse = Unirest.get(SEARCH_QUERY)
                    .queryString("key", AUTH_KEY)
                    .queryString("page", 1)
                    .queryString("page_size", 50)
                    .queryString("q", "шаверма")
                    .queryString("region_id", 38)
                    .queryString("fields", ADDITIONAL_FIELDS)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }
        JSONObject response = httpResponse.getBody().getObject();
        int itemsCount = response.getJSONObject("result").getInt("total");

        // Iterate over pages of search result (50 items per page)
        for (int page = 0; page * 50 < itemsCount; page += 1) {
            try {
                httpResponse = Unirest.get(SEARCH_QUERY)
                        .queryString("key", AUTH_KEY)
                        .queryString("page", page + 1)  // note 1-indexed pages
                        .queryString("page_size", 50)
                        .queryString("q", "шаверма")
                        .queryString("region_id", 38)
                        .queryString("fields", ADDITIONAL_FIELDS)
                        .asJson();
                processSearchResponse(httpResponse);
            } catch (UnirestException e) {
                e.printStackTrace();
                return null;
            }
        }

        return posts;
    }

    public static void main(String[] args) {
        GISCrawler crawler = new GISCrawler();
        List<Post> posts = crawler.getPosts();
        System.out.println(posts.size());
        try (PrintWriter pw = new PrintWriter("dump.txt", "UTF-16")) {
            posts.forEach(it -> pw.write(it.toString()));
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
