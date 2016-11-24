package ru.spbau.shawanation.processors;

import org.springframework.beans.factory.annotation.Autowired;
import ru.spbau.shawanation.Application;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.address.utils.PatternSearcher;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;
import ru.spbau.shawanation.database.ProcessedPost;
import ru.spbau.shawanation.ner.LocationRecognizer;
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
    private final DataBase db = new DataBase("Posts", "localhost");
    private final LocationRecognizer recognizer = new LocationRecognizer();

    public PostProcessor() throws IOException, ClassNotFoundException {}

    public List<ProcessedPost> processVKPosts() throws IOException, ClassNotFoundException {
        final List<ProcessedPost> vkPosts = new ArrayList<>();
        final List<Post> rawPosts = db.getVKPosts()
                .stream()
                .filter(item -> !IsAdvert(item.getText()))
                .collect(Collectors.toList());
        for (Post post : rawPosts) {
            final String translatedText = TextTranslator.translate(post.getText());
            Optional<String> location = PatternSearcher.getLocationFromText(post.getText());
            if (!location.isPresent()) {
                List<String> locations = recognizer.getLocations(translatedText);
                if (locations.isEmpty()) continue; // NEXT ITERATION
                location = Optional.of(locations.get(0));
            }
            Optional<PlaceCoordinates> coordinates = GeoSearcher.getLocalCityCoordinates(location.get());
            if (!coordinates.isPresent()) continue; // NEXT ITERATION
            ProcessedPost processedPost = new ProcessedPost(post.getText(), translatedText, coordinates.get());
            processedPost.setOriginalMark(post.getMark());

            db.addProcessedPost(processedPost);
        }

        return vkPosts;
    }

    private boolean IsAdvert(String text) {
        final String OFFTOP = "оффтоп".toLowerCase();
        final String ADVERT = "рекламный".toLowerCase();
        final String DRAWING = "розыгрыш".toLowerCase();
        final String searchText = text.toLowerCase();

        return searchText.contains(OFFTOP) || searchText.contains(ADVERT) || searchText.contains(DRAWING);
    }
}
