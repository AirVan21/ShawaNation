package ru.spbau;

import ru.spbau.database.PlaceCoordinates;
import ru.spbau.googleAPI.GeoSearcher;

import java.util.List;

/**
 * Start point
 */
public class Main {
    public static void main(String[] args) {
        final String input = "пр.Тореза д37, к2";
        List<PlaceCoordinates> coordinates = GeoSearcher.getCityCoordinates(input);
        coordinates.stream().forEach(item -> System.out.println(item));
    }
}
