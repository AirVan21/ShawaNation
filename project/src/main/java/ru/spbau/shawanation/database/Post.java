package ru.spbau.shawanation.database;

import com.vk.api.sdk.objects.wall.WallpostFull;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import ru.spbau.shawanation.address.googleAPI.GeoSearcher;
import ru.spbau.shawanation.address.utils.SimpleSearcher;
import ru.spbau.shawanation.utils.GlobalLogger;

import java.util.List;

/**
 * Post is class which describes wall record from vk.com
 */
@Entity("Post")
public class Post {
    @Id
    private ObjectId postId;
    private String text = "";
    private Double mark = 0.0;
    private PlaceCoordinates coordinates;

    public Post() {}

    public Post(WallpostFull post) {
        text = post.getText();
        updatePlaceCoordinates(text);
        updateMark(text);
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

    private void updatePlaceCoordinates(String text) {
        final List<String> locations = SimpleSearcher.getLocationFromText(text);
        final int FIRST = 0;

        if (!locations.isEmpty()) {
            List<PlaceCoordinates> places = GeoSearcher.getCityCoordinates(locations.get(FIRST));
            if (!places.isEmpty()) {
                coordinates = places.get(FIRST);
            }
        }
    }

    private void updateMark(String text) {
        final List<String> marks = SimpleSearcher.getMarkFromText(text);
        final int FIRST = 0;

        if (!marks.isEmpty()) {
            try {
                mark = Double.parseDouble(marks.get(FIRST));
            } catch (NumberFormatException exc) {
                GlobalLogger.log("Couldn't convert mark: " + marks.get(FIRST));
            }
        }
    }
}
