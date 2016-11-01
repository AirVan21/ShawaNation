package ru.spbau.shawanation.database;

import com.vk.api.sdk.objects.wall.WallpostFull;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.address.utils.SimpleSearcher;
import ru.spbau.shawanation.utils.GlobalLogger;
import ru.spbau.shawanation.utils.TextTranslator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Post is class which describes wall record from vk.com
 */
@Entity("Post")
public class Post {
    @Id
    private ObjectId postId;
    private String text = "";
    private String translatedText = "";
    private double mark = 0.0;
    private double sentimentMark = 0.0;
    private PlaceCoordinates coordinates;

    public Post() {}

    public Post(String text, Double mark, PlaceCoordinates coordinates) {
        this.text = text;
        this.mark = mark;
        this.coordinates = coordinates;
        updateTranslatedText();
    }

    public Post(WallpostFull post) {
        text = post.getText();
        updateTranslatedText();
        updatePlaceCoordinates();
        updateMark();
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

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public double getSentimentMark() {
        return sentimentMark;
    }

    public void setSentimentMark(double sentimentMark) {
        this.sentimentMark = sentimentMark;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append(text);
        sb.append("\n");
        if (coordinates != null) {
            sb.append(coordinates.toString());
            sb.append("\n");
        }
        sb.append(mark);
        sb.append("\n");

        return sb.toString();
    }

    public boolean isValid() {
        return !text.isEmpty();
    }

    private void updatePlaceCoordinates() {
        final List<String> locations = SimpleSearcher.getLocationFromText(text);
        final int FIRST = 0;

        if (!locations.isEmpty()) {
            List<PlaceCoordinates> places = GeoSearcher.getCityCoordinates(locations.get(FIRST));
            if (!places.isEmpty()) {
                coordinates = places.get(FIRST);
            }
        }
    }

    private void updateMark() {
        final Optional<String> markString = SimpleSearcher.getMarkFromText(text);

        if (markString.isPresent()) {
            try {
                 mark = Double.parseDouble(markString.get());
            } catch (NumberFormatException exc) {
                GlobalLogger.log("Couldn't convert mark: " + markString.get());
            }
        }
    }

    private void updateTranslatedText() {
        try {
            TextTranslator.translate(text, translatedText);
        } catch (IOException e) {
            GlobalLogger.log("Post: couldn't translate text");
        }
    }
}
