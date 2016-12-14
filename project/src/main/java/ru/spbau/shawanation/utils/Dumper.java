package ru.spbau.shawanation.utils;


import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.ProcessedPost;
import ru.spbau.shawanation.database.Venue;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Dumper {
    private final DataBase db = new DataBase("Posts", "localhost");

    public void createDumpFile() throws FileNotFoundException, UnsupportedEncodingException {
        final List<Venue> venues = db.getVenues();
        final PrintWriter writer = new PrintWriter("dump_all.txt", "UTF-8");
        ArrayList<Integer> markedCountByVenue = new ArrayList<>();
        ArrayList<Integer> unmarkedCountByVenue = new ArrayList<>();

        for (Venue venue: venues) {
            int markedCount = 0;
            int unmarkedCount = 0;
            for (ProcessedPost post : venue.getPosts()) {
                double mark = post.getOriginalMark();
                if (Double.toString((mark)).length() > 5) {
                    unmarkedCount++;
                } else {
                    markedCount++;
                }
            }
            markedCountByVenue.add(markedCount);
            unmarkedCountByVenue.add(unmarkedCount);
        }
        for (int i = 0; i < venues.size(); i++) {
            writer.write(String.format("%d, %d\n", markedCountByVenue.get(i), unmarkedCountByVenue.get(i)));
        }
        writer.close();
    }
}
