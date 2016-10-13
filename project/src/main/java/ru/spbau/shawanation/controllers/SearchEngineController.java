package ru.spbau.shawanation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbau.shawanation.database.PlaceCoordinates;
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
        List<PlaceCoordinates> coordinates = searchEngineService.getClosest(queryText, 10);
        return coordinates.stream()
                .map(c -> String.format("%s: %s,%s\n", c.getFormattedAddress(), c.getLat(), c.getLng()))
                .collect(Collectors.joining());
    }
}
