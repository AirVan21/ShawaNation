package ru.spbau.shawanation.processors;


import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.address.utils.PatternSearcher;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;
import ru.spbau.shawanation.database.ProcessedPost;
import ru.spbau.shawanation.ner.LocationRecognizer;
import ru.spbau.shawanation.services.SentimentService;
import ru.spbau.shawanation.utils.TextTranslator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PostProcessor class is a class for processing raw post
 */
public class PostProcessor {

    private SentimentService sentimentService = new SentimentService();

    private final DataBase db = new DataBase("Posts", "localhost");
    private final static String DEFAULT_ADDRESS = "St Petersburg, Russia";
    private final LocationRecognizer recognizer = new LocationRecognizer();

    public PostProcessor() throws IOException, ClassNotFoundException {}

    public List<ProcessedPost> processGISPosts() {
        final List<ProcessedPost> gisPosts = new ArrayList<>();
        final List<Post> rawPost = db.getGISPosts();
        return gisPosts;
    }

    public List<ProcessedPost> processVKPosts() throws IOException, ClassNotFoundException {
        final List<ProcessedPost> vkPosts = new ArrayList<>();
        final List<Post> rawPosts = db.getVKPosts()
                .stream()
                .filter(item -> !IsAdvert(item.getText()))
                .collect(Collectors.toList());
        for (Post post : rawPosts) {
            final String rawText = post.getText();
            final String translatedText = TextTranslator.translate(post.getText());
            final Optional<PlaceCoordinates> coordinates = processLocation(rawText, translatedText);
            if (!coordinates.isPresent()) continue; // NEXT ITERATION

            // Fill ProcessedPost object
            final ProcessedPost processedPost = new ProcessedPost(post.getText(), translatedText, coordinates.get());
            processedPost.setOriginalMark(post.getMark());
            // Store ProcessedPost object
            vkPosts.add(processedPost);
        }

        return vkPosts;
    }

    public void recalcPostsSentiment() {
        List<ProcessedPost> processedPosts = db.getProcessedPosts();
        for (ProcessedPost post : processedPosts) {
            if (Math.abs(post.getOriginalMark()) < 1e-10) {
                double sentimentMark = sentimentService.calcSentiment(post.getTranslatedText()) * 10;
                db.updateProcessedPostMark(post, sentimentMark);
            }
        }
    }

    private boolean IsAdvert(String text) {
        final String OFFTOP = "оффтоп".toLowerCase();
        final String ADVERT = "рекламный".toLowerCase();
        final String DRAWING = "розыгрыш".toLowerCase();
        final String searchText = text.toLowerCase();

        return searchText.contains(OFFTOP) || searchText.contains(ADVERT) || searchText.contains(DRAWING);
    }

    private Optional<PlaceCoordinates> processLocation(String text, String translatedText) {
        Optional<String> location = PatternSearcher.getLocationFromText(text);
        // Retrieve location from post text
        if (!location.isPresent()) {
            final List<String> textLocations = recognizer.getLocations(translatedText);
            location = textLocations.isEmpty() ? Optional.empty() : Optional.of(textLocations.get(0));
        }

        if (!location.isPresent()) {
            // Return Empty Coordinates
            return Optional.empty();
        }

        final Optional<PlaceCoordinates> coordinates = GeoSearcher.getLocalCityCoordinates(location.get());
        if (!coordinates.isPresent() || coordinates.get().getFormattedAddress().equals(DEFAULT_ADDRESS)) {
            // Handle unsuccessful coordinates search
            return Optional.empty();
        }

        return coordinates;
    }
}
