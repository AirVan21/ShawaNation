package ru.spbau.shawanation.services;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.ProcessedPost;
import ru.spbau.shawanation.database.Venue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ElasticService {
    private static final String elasticService = "http://localhost:9200";
    private static final String postsIndexName = "posts";
    private static final String postTypeName = "post";
    @Autowired
    private DataBase db;

    private void createIndex() throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .startObject("settings")
                        .field("number_of_shards", 1)
                    .endObject()
                    .startObject("mappings")
                            .startObject("post")
                                .startObject("properties")
                                    .startObject("content")
                                        .field("type", "text")
                                        .field("analyzer", "russian")
                                    .endObject()
                                    .startObject("id")
                                        .field("type", "string")
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject()
                .endObject();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.put(elasticService + "/" + postsIndexName, builder.string());
    }

    public void fullReindex() throws IOException {
        deleteIndex();

        createIndex();

        List<ProcessedPost> processedPosts = db.getProcessedPosts();
        for (ProcessedPost processedPost : processedPosts) {
            addProcessedPost(processedPost);
        }
    }

    public void addProcessedPost(ProcessedPost post) throws IOException {
        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("content", post.getText())
                    .field("id", post.getPostId().toString())
                .endObject();
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        ResponseEntity<String> response = restTemplate.postForEntity(elasticService + "/" + postsIndexName + "/post/", builder.string(), String.class);
    }

    public void deleteIndex() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return false;
                }
                return new DefaultResponseErrorHandler().hasError(response);

            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                    return;
                }
                new DefaultResponseErrorHandler().handleError(response);
            }
        });
        restTemplate.delete(elasticService + "/" + postsIndexName + "/");
    }

    public String listAll() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:9200/posts/_search?q=*:*", String.class);
        return response.getBody();
    }

    public List<Venue> queryFullText(String query) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

        XContentBuilder builder = XContentFactory.jsonBuilder()
                .startObject()
                    .field("size", 1000)
                    .startObject("query")
                        .startObject("match")
                            .field("content", query)
                        .endObject()
                    .endObject()
                .endObject();

        ResponseEntity<String> response = restTemplate.postForEntity(
                elasticService + "/" + postsIndexName + "/" + postTypeName + "/_search",
                builder.string(),
                String.class);
        JSONObject json = new JSONObject(response.getBody());
        JSONArray hits = json.getJSONObject("hits").getJSONArray("hits");

        Map<Venue, Double> scoresOfVenues = new LinkedHashMap<>();
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i).getJSONObject("_source");

            double score = hits.getJSONObject(i).getDouble("_score");
            String mongoID = hit.getString("id");

            ProcessedPost post = db.getByID(mongoID);
            Venue venue = getVenueByPost(post);

            Double venueScore = scoresOfVenues.get(venue);
            if (venueScore == null) {
                scoresOfVenues.put(venue, score);
            } else {
                scoresOfVenues.put(venue, venueScore + score);
            }
        }

        for (Map.Entry<Venue, Double> entry: scoresOfVenues.entrySet()) {
            scoresOfVenues.put(entry.getKey(), entry.getValue() * entry.getKey().getAverageMark());
        }

        List<Venue> sortedVenues = scoresOfVenues
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(Map.Entry::getValue)))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return sortedVenues;
    }

    private Map<ProcessedPost, Venue> venuesByPost = null;
    private Venue getVenueByPost(ProcessedPost post) {
        if (venuesByPost == null) {
            init();
        }
        return venuesByPost.get(post);
    }

    private void init() {
        venuesByPost = new LinkedHashMap<>();
        List<Venue> venues = db.getVenues();
        for (Venue venue : venues) {
            for (ProcessedPost processedPost : venue.getPosts()) {
                venuesByPost.put(processedPost, venue);
            }
        }
    }
}
