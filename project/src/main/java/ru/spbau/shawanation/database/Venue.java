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

    public void addPost(Post post) {
        posts.add(post);
        if (post.getMark() > 0.0) {
            averageMark += post.getMark();
            averageMark /= 2;
        }
    }

    public boolean isValid() {
        return !posts.isEmpty();
    }

    public List<Post> getPosts() {
        return posts;
    }

    public PlaceCoordinates getCoordinates() {
        return coordinates;
    }

    public double getAverageMark() {
        return averageMark;
    }
}
