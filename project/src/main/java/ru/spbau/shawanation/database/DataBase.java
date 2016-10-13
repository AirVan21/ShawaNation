package ru.spbau.shawanation.database;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.List;

/**
 * DataBase is MongoDB wrapper class
 */
public class DataBase {
    private final Datastore datastore;

    public DataBase(String name) {
        final MongoClient mongo = new MongoClient();
        datastore = new Morphia().createDatastore(mongo, name);
    }

    public void addPost(Post post) {
        datastore.save(post);
    }

    public List<Post> getPosts() {
        return datastore
                .find(Post.class)
                .asList();
    }
}
