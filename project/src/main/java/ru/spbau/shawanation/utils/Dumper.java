package ru.spbau.shawanation.utils;


import ru.spbau.shawanation.database.DataBase;
import ru.spbau.shawanation.database.ProcessedPost;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class Dumper {
    private final DataBase db = new DataBase("Posts", "localhost");

    public void createDumpFile() throws FileNotFoundException, UnsupportedEncodingException {
        final List<ProcessedPost> posts = db.getProcessedPosts();
        final PrintWriter writer = new PrintWriter("dump.txt", "UTF-8");
        for (ProcessedPost post : posts) {
            if (post.getOriginalMark() > 0) {
                writer.print(post.getOriginalMark());
                writer.print(";");
                writer.print("\"");
                String text = post.getTranslatedText();
                int indexAdddress = text.indexOf("ADDRESS");
                if (indexAdddress > 0) {
                    text = text.substring(indexAdddress);
                }
                int index = text.indexOf("\\n\\n");
                if (index > 0) {
                    text = text.substring(index);
                }
                text = text.replace("\\n", " ")
                        .replace(";", " ")
                        .replace("\"", " ");
                writer.print(text);
                writer.println("\"");
            }
        }
        writer.close();
    }
}
