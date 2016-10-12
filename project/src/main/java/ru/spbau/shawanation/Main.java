package ru.spbau.shawanation;


import ru.spbau.shawanation.crawler.Crawler;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.Post;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) {

        final String databaseName = "Posts";
        DataBase db = new DataBase(databaseName);
        if (!db.getPosts().isEmpty()) {
            return;
        }

        // Load db if db is empty()
        List<Post> posts = Crawler.getVkPosts()
                .stream()
                .filter(Post::isValid)
                .collect(Collectors.toList());
        posts.forEach(db::addPost);
    }
}
