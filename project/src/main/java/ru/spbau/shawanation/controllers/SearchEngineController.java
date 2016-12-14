package ru.spbau.shawanation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.correction.Corrector;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.ProcessedPost;
import ru.spbau.shawanation.database.Venue;
import ru.spbau.shawanation.services.SearchEngineService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
public class SearchEngineController {

    @Autowired
    private SearchEngineService searchEngineService;

    @CrossOrigin(origins = "http://localhost:3000")
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    String getClosest(@RequestParam(value = "text") String queryText) {
        Corrector corrector = new Corrector();
        String correctedQuery = corrector.getCorrection(queryText);
        String correctionMessage = "";
        if (correctedQuery == null) {
            return "<h3> Запрос сформулирован некорректно! </h3>";
        }
        if (!correctedQuery.equals(queryText)) {
            correctionMessage = "<h3> Мы считаем, что вы искали: '" + correctedQuery + "'</h3>";
        }

        Optional<PlaceCoordinates> coordinates = GeoSearcher.getLocalCityCoordinates(queryText);
        String output = "";
        if (coordinates.isPresent()) {
            output = isSaintPetersburg(coordinates.get())
                    ? getDistanceQueryDescription(coordinates.get()) + getDistanceQueryResult(coordinates.get())
                    : "<h3> Так как это сервис по поиску шавермы в Санкт-Петербурге, то мы не можем вам что-то посоветовать ;( </h3>";
        } else {
            output = getMarkQueryResult(queryText);
        }

        return correctionMessage + output;
    }

    private String getDistanceQueryResult(PlaceCoordinates placeCoordinates) {
        List<Venue> coordinates = searchEngineService.getClosest(placeCoordinates, 30);
        coordinates = sortByParameters(coordinates, placeCoordinates);
        String output = coordinates.stream()
                .map(c -> String.format("<h3> %s </h3> <b> Distance = %s </b> (%s, %s) <br> Mark = %s <br> %s ",
                        c.getCoordinates().getFormattedAddress(),
                        c.getCoordinates().getDistance(placeCoordinates.getLat(), placeCoordinates.getLng()),
                        c.getCoordinates().getLat(),
                        c.getCoordinates().getLng(),
                        c.getAverageMark(),
                        getPostHtml(c.getPosts())))
                .collect(Collectors.joining());

        return output;
    }

    private String getMarkQueryResult(String query) {
        return "<h3> Это запрос на полнотекствовый поиск </h3>";
    }

    private String getDistanceQueryDescription(PlaceCoordinates placeCoordinates) {
        return String.format("<h3> Вы ищите шаверму неподалеку от: '%s' </h3>", placeCoordinates.getFormattedAddress());
    }

    private String getVenueHtml(Venue venue) {
        return "";
    }

    private String getPostHtml(List<ProcessedPost> posts) {
        return posts
                .stream()
                .map(post -> String.format("<p> <b> Отзыв: </b> %s </p>", post.getText()))
                .collect(Collectors.joining());
    }

    private List<Venue> sortByParameters(List<Venue> venues, PlaceCoordinates coordinates) {
        if (venues.isEmpty()) {
            return venues;
        }

        double maxDist = venues.get(venues.size() - 1).getCoordinates().getDistance(coordinates.getLat(), coordinates.getLng());
        final Comparator<Venue> byDistanceAndMark = (placeOne, placeTwo) -> {
            Double firstScore = getDistanceScore(placeOne.getCoordinates(), coordinates, maxDist) + getMarkScore(placeOne.getAverageMark());
            Double secondScore = getDistanceScore(placeTwo.getCoordinates(), coordinates, maxDist) + getMarkScore(placeTwo.getAverageMark());
            return firstScore.compareTo(secondScore);
        };
        venues.sort(byDistanceAndMark);

        return venues;
    }

    private Double getDistanceScore(PlaceCoordinates input, PlaceCoordinates item, double maximum) {
        double lat = input.getLat();
        double lng = input.getLng();

        return (item.getDistance(lat, lng) / maximum) * 0.5;
    }

    private Double getMarkScore(double score) {
        final int max = 10;
        return (Math.abs(max - score) / max) * 0.5;
    }

    private boolean isSaintPetersburg(PlaceCoordinates place) {
        String address = place.getFormattedAddress().toLowerCase();
        String matchFirst = "Peterburg".toLowerCase();
        String matchSecond = "Petersburg".toLowerCase();

        return address.contains(matchFirst) || address.contains(matchSecond);
    }
}
