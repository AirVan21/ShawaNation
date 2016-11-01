package ru.spbau.shawanation.address.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SimpleSearcher is a class for searching simple tags in a strings
 */
public class SimpleSearcher {
    public final static String ADDRESS_PATTERN = "адрес:".toLowerCase();
    public final static String MARK_PATTERN = "оценка:".toLowerCase();

    public static List<String> getLocationFromText(String text) {
        return getLocationFromTextByPattern(text);
    }

    public static Optional<String> getMarkFromText(String text) {
        return getMarkFromTextByPattern(text);
    }

    private static List<String> getLocationFromTextByPattern(String text) {
        return Arrays
                .stream(text.split("\\r?\\n")) // split on new line
                .map(String::toLowerCase)
                .filter(line -> line.contains(ADDRESS_PATTERN)) // get string with address
                .map(line -> line.substring(line.indexOf(":") + 1)) // get's only address from address string
                .collect(Collectors.toList());
    }

    private static Optional<String> getMarkFromTextByPattern(String text) {
        List<String> marks = Arrays
                .stream(text.split("\\r?\\n")) // split on new line
                .map(String::toLowerCase)      //
                .filter(line -> line.contains(MARK_PATTERN))
                .map(line -> line.substring(line.indexOf(":") + 1).trim())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

//                        && line.contains(" ") )    // filter non-patter strings
//                .map(line -> line.substring(0, line.indexOf(" ")))         // get's first mark
//                .map(line -> line.replace(",", "."))                       // "." is used for double parse
//                .collect(Collectors.toList());
        return marks.isEmpty() ? Optional.empty() : Optional.of(marks.get(0));
    }

    private static String matchMarkPattern(String markText) {
        String result = "";

        // For String to Double conversion
        markText = markText.replace(",", ".");

        // Cases for:
        // a) "7.5 из 10" (take "7.5")
        // b) 7-7.5 из 10 (take "7")
        if (markText.contains(" ")) {
            // already knows tht " " is presented
            result = markText.substring(0, markText.indexOf(" "));
        }

        // return empty string nothing matched
        return result;
    }
}
