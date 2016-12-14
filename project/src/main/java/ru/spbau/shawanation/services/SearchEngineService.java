package ru.spbau.shawanation.services;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.spbau.shawanation.crawler.Crawler;
import ru.spbau.shawanation.crawler.FoursquareCrawler;
import ru.spbau.shawanation.crawler.GISCrawler;
import ru.spbau.shawanation.crawler.VKCrawler;
import ru.spbau.shawanation.database.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchEngineService {
    @Autowired
    private DataBase db;

    public void fillDatabase() {
        loadDataFromCrawler(new VKCrawler());
        loadDataFromCrawler(new GISCrawler());
        loadDataFromCrawler(new FoursquareCrawler());
    }

    public void storeVenues() {
        final List<Venue> venues = buildVenues();
        venues.forEach(venue -> db.addVenue(venue));
    }

    public void dropCollection(Class collection) {
        db.dropCollection(collection);
    }

    public List<Venue> getClosest(PlaceCoordinates current, int count) {
        final double lat = current.getLat();
        final double lng = current.getLng();
        final Comparator<Venue> byDistance = (placeOne, placeTwo) ->
                placeOne.getCoordinates().getDistance(lat, lng).compareTo(placeTwo.getCoordinates().getDistance(lat, lng));
        return db.getVenues()
                .stream()
                .sorted(byDistance)
                .limit(count)
                .collect(Collectors.toList());
    }

    private List<Venue> buildVenues() {
        List<ProcessedPost> posts = db.getProcessedPosts();
        Set<ObjectId> usedPosts = new HashSet<>();
        List<Venue> venues = new ArrayList<>();

        for (ProcessedPost post : posts) {
            if (usedPosts.contains(post.getPostId())) {
                continue;
            }
            usedPosts.add(post.getPostId());

            Venue venue = new Venue(post);
            posts
                    .stream()
                    .filter(item -> !usedPosts.contains(item.getPostId()))
                    .filter (item -> AreClose(venue.getCoordinates(), item.getCoordinates()))
                    .forEach(item -> {
                            usedPosts.add(item.getPostId());
                            venue.addPost(item);
                    });
            venues.add(venue);
        }

        return venues;
    }

    private boolean AreClose(PlaceCoordinates left, PlaceCoordinates right) {
        final double distance = 30;
        return left.getDistance(right.getLat(), right.getLng()) < distance;
    }

    private void loadDataFromCrawler(Crawler crawler) {
        final List<Post> posts = crawler.getPosts();
        posts.forEach(db::addPost);
    }
}
