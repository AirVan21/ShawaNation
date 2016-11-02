package ru.spbau.shawanation.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


/**
 * Post is class which describes wall record from vk.com
 */
@Entity("Post")
public class Post {
    @Id
    private ObjectId postId;
    private String text = "";
    private double mark;
    private PlaceCoordinates coordinates;

    public Post() {}

    public Post(String text, Double mark, PlaceCoordinates coordinates) {
        this.text = text;
        this.mark = (mark == null || mark.isNaN()) ? 0.0 : mark;
        this.coordinates = (coordinates == null) ? new PlaceCoordinates() : coordinates;
    }

    public ObjectId getPostId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public PlaceCoordinates getCoordinates() {
        return coordinates;
    }

    public Double getMark() {
        return mark;
    }

    public boolean isValid() {
        return !text.isEmpty() && coordinates != null;
    }

    public boolean isRelated(Post other) {
        // Distance in meters
        final double aggregateDistance = 50;
        PlaceCoordinates otherCoordinates = other.getCoordinates();

        return coordinates.getDistance(otherCoordinates.getLat(), otherCoordinates.getLng()) < aggregateDistance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Text: ").append(text).append("\n");
        if (coordinates != null) {
            sb.append("Coordinates: ").append(coordinates.toString()).append("\n");
        }
        sb.append("Mark: ").append(mark).append("\n");

        return sb.toString();
    }

}
