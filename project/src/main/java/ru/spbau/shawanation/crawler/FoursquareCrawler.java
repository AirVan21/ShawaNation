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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FoursquareCrawler implements Crawler {

    private static final String API_DOMEN = "https://api.foursquare.com";
    private static final String API_VERSION = "v2";
    private static final String VENUES_SERVICE = "venues";
    private static final String EXPLORE_METHOD = "explore";
    private static final String NEAR = "СПб, Россия";
    private static final String NEAR_GEOID = "72057594038426753";
    private static final String QUERY = "шаверма";
    private static final String CLIENT_ID = "ANUQION4MYVNJ1VNU4U0YJXKDDC43KYEX5KVALXWUZZLBNPZ";
    private static final String CLIENT_SECRET = "O02U3XW0GIMSY5CQWIC4BPOZVFYEYMD3V4ZH3ZYZIQ3URBFF";
    private static final String RADIUS = "100000";
    private static final String DATE;
    private static final String VENUES_REQUEST = API_DOMEN + "/" + API_VERSION + "/" + VENUES_SERVICE;
    private static final String TIPS_METHOD = "tips";

    private final List<Post> posts = new ArrayList<>();

    static {
        DATE = new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    @Override
    public List<Post> getPosts() {
        HttpResponse<JsonNode> httpResponse;
        try {
            httpResponse = Unirest.get(VENUES_REQUEST + "/" + EXPLORE_METHOD)
                    .queryString("near", NEAR)
                    .queryString("nearGeoId", NEAR_GEOID)
                    .queryString("query", QUERY)
                    .queryString("client_id", CLIENT_ID)
                    .queryString("client_secret", CLIENT_SECRET)
                    .queryString("radius", RADIUS)
                    .queryString("v", DATE)
                    .queryString("limit", 50)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }

        int totalResults = httpResponse
                .getBody()
                .getObject()
                .getJSONObject("response")
                .getInt("totalResults");

        // Iterate over items, 50 per page
        for (int offset = 0; offset < totalResults; offset += 50) {
            try {
                httpResponse = Unirest.get(VENUES_REQUEST + "/" + EXPLORE_METHOD)
                        .queryString("near", NEAR)
                        .queryString("nearGeoId", NEAR_GEOID)
                        .queryString("query", QUERY)
                        .queryString("client_id", CLIENT_ID)
                        .queryString("client_secret", CLIENT_SECRET)
                        .queryString("radius", RADIUS)
                        .queryString("v", DATE)
                        .queryString("limit", 50)
                        .queryString("offset", offset)
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
                return null;
            }
            processResponse(httpResponse.getBody().getObject());
        }

        return posts;
    }

    private void processResponse(JSONObject response) {
        JSONArray groups = response.getJSONObject("response").getJSONArray("groups");

        for (int i = 0; i < groups.length(); i++) {
            JSONObject currentGroup = groups.getJSONObject(i);
            JSONArray venues = currentGroup.getJSONArray("items");
            for (int j = 0; j < venues.length(); j++) {
                JSONObject curVenue = venues.getJSONObject(j).getJSONObject("venue");
                processVenue(curVenue);
            }
        }

    }

    private void processVenue(JSONObject curVenue) {
        String name = curVenue.getString("name");
        double lat = curVenue.getJSONObject("location").optDouble("lat");
        double lon = curVenue.getJSONObject("location").optDouble("lng");
        double rating = curVenue.optDouble("rating");
        String id = curVenue.getString("id");

        // Foursquare is very reluctant to give all tips in explore-request,
        // so we have to make specific tips-request for each venue
        List<String> tips = getAllTips(id);

        if (tips == null) return;

        for (String tipText : tips) {
            posts.add(new Post(tipText, rating, new PlaceCoordinates(lat, lon)));
        }
    }

    private List<String> getAllTips(String venueId) {
        HttpResponse<JsonNode> httpResponse;
        ArrayList<String> tips = new ArrayList<>();

        try {
            httpResponse = Unirest
                    .get(VENUES_REQUEST + "/" + venueId + "/" + TIPS_METHOD)
                    .queryString("client_id", CLIENT_ID)
                    .queryString("client_secret", CLIENT_SECRET)
                    .queryString("v", DATE)
                    .asJson();
        } catch (UnirestException e) {
            e.printStackTrace();
            return null;
        }

        int tipsCount = httpResponse
                .getBody()
                .getObject()
                .getJSONObject("response")
                .getJSONObject("tips")
                .getInt("count");

        // Iterating over tips, 500 per page
        for (int offset = 0; offset < tipsCount; offset += 500) {
            try {
                httpResponse = Unirest
                        .get(VENUES_REQUEST + "/" + venueId + "/" + TIPS_METHOD)
                        .queryString("client_id", CLIENT_ID)
                        .queryString("client_secret", CLIENT_SECRET)
                        .queryString("v", DATE)
                        .queryString("limit", 500)
                        .queryString("offset", offset)
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
                return null;
            }

            addTipsFromResponse(tips, httpResponse.getBody().getObject());
        }

        return tips;
    }

    private void addTipsFromResponse(ArrayList<String> tips, JSONObject object) {
        JSONArray items = object
                .getJSONObject("response")
                .getJSONObject("tips")
                .getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject curTip = items.getJSONObject(i);
            String text = curTip.getString("text");
            tips.add(text);
        }
    }

    public static void main(String[] args) {
        FoursquareCrawler crawler = new FoursquareCrawler();
        List<Post> posts = crawler.getPosts();

        System.out.println(posts.size());
        try (PrintWriter pw = new PrintWriter("dump.txt", "UTF-16")) {
            for (Post post : posts) {
                pw.write(post.toString() + "\n");
                pw.write("================\n");
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
