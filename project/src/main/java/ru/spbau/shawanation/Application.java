package ru.spbau.shawanation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.PropertySource;
import ru.spbau.shawanation.services.SearchEngineService;

import java.util.stream.Collectors;


/**
 * Main class
 */
@PropertySource("classpath:application.properties")
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private SearchEngineService searchEngineService;

    @Override
    public void run(String... args) {
        if (args.length == 0) {
            System.out.println("Not enough arguments - provide search query, please");
            return;
        }

        searchEngineService.getClosest(args[0], 10).stream()
                .filter(item -> !item.getFormattedAddress().isEmpty())
                .map(item -> String.format("%s: %s,%s\n", item.getFormattedAddress(), item.getLat(), item.getLng()))
                .forEach(System.out::println);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
