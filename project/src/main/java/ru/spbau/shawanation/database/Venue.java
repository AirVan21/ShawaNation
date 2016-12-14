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
    private List<ProcessedPost> posts = new ArrayList<>();
    private PlaceCoordinates coordinates = new PlaceCoordinates();
    private double averageMark = 0;

    public Venue() {}

    public Venue(ProcessedPost post) {
        posts.add(post);
        coordinates = post.getCoordinates();
        averageMark = post.getOriginalMark();
    }

    public Venue(List<ProcessedPost> posts, PlaceCoordinates coordinates, double mark) {
        this.posts = posts;
        this.coordinates = coordinates;
        averageMark = mark;
    }

    public void addPost(ProcessedPost post) {
        posts.add(post);
        if (post.getOriginalMark() > 0.0) {
            averageMark += post.getOriginalMark();
            averageMark /= 2;
        }
    }

    public boolean isValid() {
        return !posts.isEmpty();
    }

    public List<ProcessedPost> getPosts() {
        return posts;
    }

    public PlaceCoordinates getCoordinates() {
        return coordinates;
    }

    public double getAverageMark() {
        return averageMark;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Venue venue = (Venue) o;

        return coordinates != null ? coordinates.equals(venue.coordinates) : venue.coordinates == null;
    }

    @Override
    public int hashCode() {
        return coordinates != null ? coordinates.hashCode() : 0;
    }
}
