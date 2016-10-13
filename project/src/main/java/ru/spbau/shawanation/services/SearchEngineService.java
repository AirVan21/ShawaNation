package ru.spbau.shawanation.services;

import org.springframework.stereotype.Service;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Post;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchEngineService {
    private static final String databaseName = "Posts";
    private final DataBase db = new DataBase(databaseName);

    public List<PlaceCoordinates> getClosest(String address, int count) {
        final double lat = 59.933855;
        final double lng = 30.306359;

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
