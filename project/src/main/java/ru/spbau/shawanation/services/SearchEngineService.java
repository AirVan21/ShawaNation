package ru.spbau.shawanation.services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.crawler.Crawler;
import ru.spbau.shawanation.crawler.FoursquareCrawler;
import ru.spbau.shawanation.crawler.GISCrawler;
import ru.spbau.shawanation.crawler.VKCrawler;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;
import ru.spbau.shawanation.database.Venue;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchEngineService {
    @Autowired
    private DataBase db;

    public List<PlaceCoordinates> getClosest(String address, int count) {
        final List<PlaceCoordinates> current = GeoSearcher.getCityCoordinates(address);
        if (current.isEmpty()) {
            return new ArrayList<>();
        }

        final double lat = current.get(0).getLat();
        final double lng = current.get(0).getLng();
        final Comparator<PlaceCoordinates> byDistance = (placeOne, placeTwo) ->
                placeOne.getDistance(lat, lng).compareTo(placeTwo.getDistance(lat, lng));

        return db.getVenues()
                .stream()
                .map(Venue::getCoordinates)
                .distinct()
                .sorted(byDistance)
                .limit(count)
                .collect(Collectors.toList());
    }

    public void fillDatabase() {
        db.dropDatabase();

        loadDataFromCrawler(new VKCrawler());
        loadDataFromCrawler(new GISCrawler());
        // Should be filtered
//        loadDataFromCrawler(new FoursquareCrawler());
    }

    public void storeVenues() {
        db.dropCollection(Venue.class);
        final List<Venue> venues = buildVenues();
        venues.forEach(venue -> db.addVenue(venue));
    }

    private List<Venue> buildVenues() {
        List<Post> posts = db.getPosts();
        Set<ObjectId> usedPosts = new HashSet<>();
        List<Venue> venues = new ArrayList<>();

        for (Post post : posts) {
            if (usedPosts.contains(post.getPostId())) {
                continue;
            }
            usedPosts.add(post.getPostId());

            Venue venue = new Venue(post);
            posts
                    .stream()
                    .filter(item -> !usedPosts.contains(item.getPostId()))
                    .filter(item -> item.isRelated(post))
                    .forEach(item -> {
                            usedPosts.add(item.getPostId());
                            venue.addPost(item);
                    });
            venues.add(venue);
        }

        return venues;
    }

    private void loadDataFromCrawler(Crawler crawler) {
        final List<Post> posts = crawler.getPosts();
        posts.forEach(db::addPost);
    }

    public void showVenues() {
        List<Venue> venues = db.getVenues();
        venues.forEach(venue -> System.out.println(venue.getCoordinates()));
    }
}
