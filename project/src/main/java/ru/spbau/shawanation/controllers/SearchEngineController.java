package ru.spbau.shawanation.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.spbau.shawanation.services.SearchEngineService;

@RestController
public class SearchEngineController {

    @Autowired
    private SearchEngineService searchEngineService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    String query() {
        return searchEngineService.query();
    }
}
