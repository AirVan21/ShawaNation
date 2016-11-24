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
    private double originalMark = 0;
    private double sentimentMark = 0;
    private double mixedMark = 0;

    private PlaceCoordinates coordinates;

    public ProcessedPost() {}

    public ProcessedPost(String text, String translatedText, PlaceCoordinates coordinates) {
        this.text = text;
        this.translatedText = translatedText;
        this.coordinates = coordinates;
    }

    public double getOriginalMark() {
        return originalMark;
    }

    public void setOriginalMark(double originalMark) {
        this.originalMark = originalMark;
    }

    public double getSentimentMark() {
        return sentimentMark;
    }

    public void setSentimentMark(double sentimentMark) {
        this.sentimentMark = sentimentMark;
    }

    public double getMixedMark() {
        return mixedMark;
    }

    public void setMixedMark(double mixedMark) {
        this.mixedMark = mixedMark;
    }
}
