package ru.spbau.shawanation.address.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PatternSearcher is a class for searching simple tags in a strings
 */
public class PatternSearcher {
    private final static String ADDRESS_PATTERN = "адрес:".toLowerCase();
    private final static String MARK_PATTERN = "оценка:".toLowerCase();

    public static Optional<String> getLocationFromText(String text) {
        return getLocationFromTextByPattern(text);
    }

    public static Optional<String> getMarkFromText(String text) {
        return getMarkFromTextByPattern(text);
    }

    private static Optional<String> getLocationFromTextByPattern(String text) {
        List<String> addresses = Arrays
                .stream(text.split("\\r?\\n"))                             // split on new line
                .map(String::toLowerCase)
                .filter(line -> line.contains(ADDRESS_PATTERN))            // get string with address
                .map(line -> line.substring(line.indexOf(":") + 1).trim()) // get's only address from address string
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());

        return addresses.isEmpty() ? Optional.empty() : Optional.of(addresses.get(0));
    }

    private static Optional<String> getMarkFromTextByPattern(String text) {
        // Gets lines with marks
        List<String> marks = Arrays
                .stream(text.split("\\r?\\n")) // split on new line
                .map(String::toLowerCase)      //
                .filter(line -> line.contains(MARK_PATTERN))
                .map(line -> line.substring(line.indexOf(":") + 1).trim())
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toList());
        // Parses different mark cases
        marks = marks
                .stream()
                .map(PatternSearcher::matchMarkPattern)
                .filter(mark -> !mark.isEmpty())
                .collect(Collectors.toList());

        return marks.isEmpty() ? Optional.empty() : Optional.of(marks.get(0));
    }

    private static String matchMarkPattern(String markText) {
        String result;
        // For String to Double conversion
        markText = markText.replace(",", ".");

        // Cases for:
        // a) "7.5 из 10" (take "7.5")
        // b) "7-7.5 из 10" (take "7")
        if (markText.contains(" ")) {
            // already knows that " " is presented
            result = markText.substring(0, markText.indexOf(" "));
            return result.contains("-") ? result.substring(0, markText.indexOf("-")) : result;
        }

        // Case for:
        // c) 9/10
        if (markText.contains("/")) {
            result = markText.substring(0, markText.indexOf("/"));
            return result;
        }

        // Case for:
        // d) 9\10
        if (markText.contains("\\")) {
            result = markText.substring(0, markText.indexOf("\\"));
            return result;
        }

        // Case for:
        // e) 5
        result = markText;

        return result;
    }
}
