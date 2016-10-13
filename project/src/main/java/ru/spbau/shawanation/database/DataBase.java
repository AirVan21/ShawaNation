package ru.spbau.shawanation.database;

import com.mongodb.MongoClient;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
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
        datastore.save(post);
    }

    public List<Post> getPosts() {
        return datastore
                .find(Post.class)
                .asList();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
