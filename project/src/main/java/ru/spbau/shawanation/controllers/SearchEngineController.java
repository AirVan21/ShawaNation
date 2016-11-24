package ru.spbau.shawanation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbau.shawanation.correction.Corrector;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.database.Venue;
import ru.spbau.shawanation.services.SearchEngineService;

import java.util.List;
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
        String correctionMessage;
        if (correctedQuery == null) {
            correctionMessage = "No suggestions, try to reformulate your query.<br>";
            correctedQuery = queryText; // try to find using primary query nevertheless
        } else if (correctedQuery.equals(queryText)) {
            correctionMessage = "";
        } else {
            correctionMessage = "Maybe you looked for: '" + correctedQuery + "'<br>";
        }

        List<Venue> coordinates = searchEngineService.getClosest(correctedQuery, 10);
        return coordinates.stream()
                .map(c -> String.format("<h4> %s </h4> <b> Mark = %s </b>, %s, %s <br> %s", c.getCoordinates().getFormattedAddress(),
                        c.getAverageMark(), c.getCoordinates().getLat(), c.getCoordinates().getLng(), getHtml(c)))
                .collect(Collectors.joining())
                + correctionMessage;
    }

    private String getHtml(Venue venue) {
        return venue.getPosts()
                .stream()
                .map(post -> String.format("<p> %s </p> <br>", post.getText()))
                .collect(Collectors.joining());
    }
}
