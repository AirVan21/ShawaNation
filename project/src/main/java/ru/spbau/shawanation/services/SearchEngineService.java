package ru.spbau.shawanation.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchEngineService {
    private static final String databaseName = "Posts";
    
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

        return db.getPosts()
                .stream()
                .filter(Post::isValid)
                .map(Post::getCoordinates)
                .sorted(byDistance)
                .limit(count)
                .collect(Collectors.toList());
    }
}
