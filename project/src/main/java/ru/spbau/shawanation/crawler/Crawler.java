package ru.spbau.shawanation.crawler;

import ru.spbau.shawanation.database.Post;

import java.util.List;

public interface Crawler {
    List<Post> getPosts();
}
