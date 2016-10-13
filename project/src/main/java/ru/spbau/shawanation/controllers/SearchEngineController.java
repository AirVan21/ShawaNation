package ru.spbau.shawanation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.services.SearchEngineService;

import java.util.List;

@RestController
public class SearchEngineController {

    @Autowired
    private SearchEngineService searchEngineService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    String getClosest(@RequestBody String queryText) {
        List<PlaceCoordinates> coordinates = searchEngineService.getClosest(queryText, 10);
        return "";
    }
}
