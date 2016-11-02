package ru.spbau.shawanation.crawler;

import com.google.gson.Gson;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;

import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.wall.WallpostFull;
import com.vk.api.sdk.objects.wall.responses.GetResponse;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.address.utils.PatternSearcher;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;
import ru.spbau.shawanation.utils.GlobalLogger;
import ru.spbau.shawanation.utils.TextTranslator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Crawler is a class for collecting information from web
 *
 * Currently it just pulls data from specific VK group
 * using VK API
 */
public class VKCrawler implements Crawler {
    private static int GROUP_ID = -94119361;

    /**
     * Gets vk posts sending VkApi getClosest
     */
    public List<Post> getPosts() {
        final TransportClient transportClient = HttpTransportClient.getInstance();
        final VkApiClient vk = new VkApiClient(transportClient, new Gson());
        final List<Post> result = new ArrayList<>();

        int offset = 0;
        while (true) {
            try {
                GetResponse getResponse = vk.wall()
                        .get()
                        .ownerId(GROUP_ID)
                        .count(100)
                        .offset(offset)
                        .execute();
                if (getResponse.getItems().isEmpty()) {
                    break;
                }

                for (WallpostFull post : getResponse.getItems()) {
                    final String text = post.getText();
                    final Optional<Double> mark = getMark(text);
                    final Optional<PlaceCoordinates> coordinates = getPlaceCoordinates(text);
                    if (mark.isPresent() && coordinates.isPresent()) {
                        result.add(new Post(text, mark.get(), coordinates.get()));
                    }
                }
                // Next data chunk
                offset += 100;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private Optional<PlaceCoordinates> getPlaceCoordinates(String text) {
        final Optional<String> location = PatternSearcher.getLocationFromText(text);
        List<PlaceCoordinates> places = new ArrayList<>();

        if (location.isPresent()) {
            places = GeoSearcher.getCityCoordinates(location.get());
        }

        return places.isEmpty() ? Optional.empty() : Optional.of(places.get(0));
    }

    private Optional<Double> getMark(String text) {
        final Optional<String> markString = PatternSearcher.getMarkFromText(text);
        Optional<Double> mark = Optional.empty();

        if (markString.isPresent()) {
            try {
                mark = Optional.of(Double.parseDouble(markString.get()));
            } catch (NumberFormatException exc) {
                GlobalLogger.log("Couldn't convert mark: " + markString.get());
            }
        }

        return mark;
    }
}
