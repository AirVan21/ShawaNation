package ru.spbau.shawanation.database;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * ProcessedPost is a class which
 */
@Entity("ProcessedPost")
public class ProcessedPost {
    @Id
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

    public ObjectId getPostId() {
        return postId;
    }

    public void setPostId(ObjectId postId) {
        this.postId = postId;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public PlaceCoordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(PlaceCoordinates coordinates) {
        this.coordinates = coordinates;
    }
}
