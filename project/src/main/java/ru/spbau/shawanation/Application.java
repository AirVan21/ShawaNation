package ru.spbau.shawanation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import ru.spbau.shawanation.database.PlaceCoordinates;
import ru.spbau.shawanation.services.SearchEngineService;

import java.util.List;


/**
 * Main class
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private SearchEngineService searchEngineService;

    @Override
    public void run(String... args) {
        List<PlaceCoordinates> coordinatesList = searchEngineService.getClosest("ул. Хлопина, д.8, корпус 3, лит. А", 10);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
