package ru.spbau.utility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by airvan21 on 24.09.16.
 */
public class AddressSearcher {
    public final static String ADDRESS_PATTERN = "адрес:";
    public final static String MARK_PATTERN   = "оценка:";

    public static List<String> getLocationFromText(String text) {
        return getLocationFromTextByPattern(text);
    }

    private static List<String> getLocationFromTextByPattern(String text) {
        return Arrays
                .stream(text.split("\\r?\\n")) // split on new line
                .map(String::toLowerCase)
                .filter(line -> line.contains(ADDRESS_PATTERN))
                .map(line -> line.substring(line.indexOf(":") + 1)) // get's only address from address string
                .collect(Collectors.toList());
    }


}
