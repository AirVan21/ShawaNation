package ru.spbau.shawanation.database;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DataBase is MongoDB wrapper class
 */
@Service
public class DataBase {
    private Datastore datastore;

    public DataBase(@Value("${mongo.dbName}") String datastoreName,
                    @Value("${mongo.host}") String host) {
        final MongoClient mongo = new MongoClient(host);
        datastore = new Morphia().createDatastore(mongo, datastoreName);
    }

    public void addPost(Post post) {
        if (post.isValid()) {
            datastore.save(post);
        }
    }

    public void addProcessedPost(ProcessedPost post) {
        datastore.save(post);
    }

    public void updateProcessedPostMark(ProcessedPost post, double mark) {
        UpdateOperations<ProcessedPost> update = datastore
                .createUpdateOperations(ProcessedPost.class)
                .set("originalMark", mark);
        datastore.update(post, update);
    }

    public void addVenue(Venue venue) {
        if (venue.isValid()) {
            datastore.save(venue);
        }
    }

    public List<Post> getPosts() {
        return datastore
                .find(Post.class)
                .asList();
    }

    public List<Post> getVKPosts() {
        return datastore
                .find(Post.class)
                .field("type")
                .equal(Post.PostType.VK)
                .asList();
    }

    public List<Post> getGISPosts() {
        return datastore
                .find(Post.class)
                .field("type")
                .equal(Post.PostType.GIS)
                .asList();
    }

    public List<Post> getSquarePosts() {
        return datastore
                .find(Post.class)
                .field("type")
                .equal(Post.PostType.SQUARE)
                .asList();
    }

    public List<ProcessedPost> getProcessedPosts() {
        return datastore
                .find(ProcessedPost.class)
                .asList();
    }

    public List<Venue> getVenues() {
        return datastore
                .find(Venue.class)
                .asList();
    }
    
    public void dropCollection(Class source) {
        datastore.getCollection(source).drop();
    }

    public void dropDatabase() {
        datastore.getDB().dropDatabase();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
