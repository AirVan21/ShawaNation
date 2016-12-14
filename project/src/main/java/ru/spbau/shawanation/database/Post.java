package ru.spbau.shawanation.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


/**
 * Post is class which describes simple post
 */
@Entity("Post")
public class Post {
    public enum PostType {
        VK,
        GIS,
        SQUARE
    }

    public void setPostId(ObjectId postId) {
        this.postId = postId;
    }

    @Id
    private ObjectId postId;
    private String text = "";
    private double mark;
    private PlaceCoordinates coordinates;
    private PostType type;


    public Post() {}

    public Post(String text, Double mark, PlaceCoordinates coordinates, PostType type) {
        this.text = text;
        this.mark = (mark == null || mark.isNaN()) ? 0.0 : mark;
        this.coordinates = (coordinates == null) ? new PlaceCoordinates() : coordinates;
        this.type = type;
    }

    public ObjectId getPostId() {
        return postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public PlaceCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(PlaceCoordinates coordinates) {
        this.coordinates = coordinates;
    }

    public Double getMark() {
        return mark;
    }

    public void setMark(double mark) {
        this.mark = mark;
    }

    public PostType getType() {
        return type;
    }

    public void setType(PostType type) {
        this.type = type;
    }

    public boolean isValid() {
        return !text.isEmpty();
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
