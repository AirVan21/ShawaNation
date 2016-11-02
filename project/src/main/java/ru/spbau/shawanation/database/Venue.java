package ru.spbau.shawanation.database;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Reference;

import java.util.ArrayList;
import java.util.List;

/**
 * Venue is a class which aggregates posts about same restaurants
 */
@Entity("Venue")
public class Venue {
    @Reference
    private List<Post> posts = new ArrayList<>();
    private PlaceCoordinates coordinates = new PlaceCoordinates();
    private double averageMark = 0;

    public Venue() {}

    public Venue(Post post) {
        posts.add(post);
        coordinates = post.getCoordinates();
        averageMark = post.getMark();
    }

    public Venue(List<Post> posts, PlaceCoordinates coordinates, double mark) {
        this.posts = posts;
        this.coordinates = coordinates;
        averageMark = mark;
    }
}
