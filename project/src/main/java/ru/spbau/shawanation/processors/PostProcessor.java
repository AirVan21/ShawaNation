package ru.spbau.shawanation.processors;

import org.springframework.beans.factory.annotation.Autowired;
import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.Post;
import ru.spbau.shawanation.database.ProcessedPost;

import java.util.ArrayList;
import java.util.List;

/**
 * PostProcessor class is a class for processing raw post
 */
public class PostProcessor {
    @Autowired
    private DataBase db;

    public List<ProcessedPost> processVKPosts() {
        final List<ProcessedPost> vkPosts = new ArrayList<>();
        List<Post> rawPosts = db.getVKPosts();

        return vkPosts;
    }
}
