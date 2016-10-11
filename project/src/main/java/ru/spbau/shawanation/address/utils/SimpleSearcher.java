package ru.spbau.shawanation.address.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SimpleSearcher is a class for searching simple tags in a strings
 */
public class SimpleSearcher {
    public final static String ADDRESS_PATTERN = "адрес:";
    public final static String MARK_PATTERN = "оценка:";

    public static List<String> getLocationFromText(String text) {
        return getInformationFromTextByPattern(text, ADDRESS_PATTERN);
    }

    public static List<String> getMarkFromText(String text) {
        return getInformationFromTextByPattern(text, MARK_PATTERN);
    }

    private static List<String> getInformationFromTextByPattern(String text, String pattern) {
        return Arrays
                .stream(text.split("\\r?\\n")) // split on new line
                .map(String::toLowerCase)
                .filter(line -> line.contains(pattern))
                .map(line -> line.substring(line.indexOf(":") + 1)) // get's only address from address string
                .collect(Collectors.toList());
    }
}
