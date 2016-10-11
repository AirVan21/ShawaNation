package ru.spbau.shawanation;


import ru.spbau.shawanation.crawler.Crawler;
import ru.spbau.shawanation.database.Post;

import java.util.List;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) {
        List<Post> posts = Crawler.getVkPosts();
        posts.forEach(System.out::println);
    }
}
