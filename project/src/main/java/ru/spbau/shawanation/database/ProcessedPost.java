package ru.spbau.shawanation.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

/**
 * ProcessedPost is a class which
 */
@Entity("Post")
public class ProcessedPost {
    private ObjectId postId;
    private String text = "";
    private String translatedText = "";
    private double mark;
    private PlaceCoordinates coordinates;

    public ProcessedPost() {}

    public ProcessedPost(String text, String translatedText, double mark, PlaceCoordinates coordinates) {
        this.text = text;
        this.translatedText = translatedText;
        this.mark = mark;
        this.coordinates = coordinates;
    }
}
