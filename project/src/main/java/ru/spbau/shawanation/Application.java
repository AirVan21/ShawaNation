package ru.spbau.shawanation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;
import ru.spbau.shawanation.crawler.Crawler;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.Post;
import ru.spbau.shawanation.services.SearchEngineService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class
 */
@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private SearchEngineService searchEngineService;

    @Override
    public void run(String... args) {
        System.out.println(searchEngineService.query());

//        final String databaseName = "Posts";
//        DataBase db = new DataBase(databaseName);
//        if (!db.getPosts().isEmpty()) {
//            return;
//        }
//
//        // Load db if db is empty()
//        List<Post> posts = Crawler.getVkPosts()
//                .stream()
//                .filter(Post::isValid)
//                .collect(Collectors.toList());
//        posts.forEach(db::addPost);
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
