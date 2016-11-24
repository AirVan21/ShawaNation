package ru.spbau.shawanation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.correction.Corrector;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.ProcessedPost;
import ru.spbau.shawanation.database.Venue;
import ru.spbau.shawanation.services.SearchEngineService;

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
            return "<h3> Ваш запрос не  дал результатов! </h3>";
        }
        if (!correctedQuery.equals(queryText)) {
            correctionMessage = "<h3> Может вы искали: '" + correctedQuery + "'</h3>";
        }

        Optional<PlaceCoordinates> coordinates = GeoSearcher.getLocalCityCoordinates(queryText);
        String output = coordinates.isPresent()
                ? getDistanceQueryDescription(coordinates.get()) + getDistanceQueryResult(coordinates.get())
                : getMarkQueryResult(queryText);

        return correctionMessage + output;
    }

    private String getDistanceQueryResult(PlaceCoordinates placeCoordinates) {
        List<Venue> coordinates = searchEngineService.getClosest(placeCoordinates, 10);
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
}
