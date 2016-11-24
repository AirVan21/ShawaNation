package ru.spbau.shawanation.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

/**
 * TextTranslator is a class for text translation (uses Yandex API)
 */
public class TextTranslator {
    public final  static String ENGLISH_LANGUAGE = "en";
    private final static String ADDRESS = "https://translate.yandex.net/api/v1.5/tr.json/translate?";
    private final static String FREE_YANDEX_KEY = "key=trnsl.1.1.20150627T071448Z.117dacaac1e63b79.6b1b4bb84635161fcd400dace9fb2220d6f344ef";
    private final static String REQUEST_METHOD = "POST";
    private final static String ENCODING = "UTF-8";
    private final static String TARGET_LANGUAGE = "en";

    /**
     * Translates input text to target language
     * @param text - source text
     * @return translated text
     * @throws IOException
     */
    public static String translate(String text) throws IOException {
        // Builds URL
        StringBuilder sb = new StringBuilder();
        sb.append(ADDRESS);
        sb.append(FREE_YANDEX_KEY);
        URL url = new URL(sb.toString());
        // Sets connection
        HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
        connection.setRequestMethod(REQUEST_METHOD);
        connection.setDoOutput(true);
        // Builds request
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.writeBytes("text=" + URLEncoder.encode(text, ENCODING));
        dataOutputStream.writeBytes("&lang=" + TARGET_LANGUAGE);
        dataOutputStream.flush();
        // Returns result
        return parseOutput(connection.getInputStream());
    }

    /**
     * Parses input stream from Yandex to result string
     * @param response - response from Yandex API
     * @return result string
     */
    private static String parseOutput(InputStream response) {
        String defaultResult = "";
        String json = new Scanner(response).nextLine();
        if (json.isEmpty()) {
            return defaultResult;
        }
        // Gets indexes for substring
        int start = json.indexOf("[");
        int end = json.indexOf("]");
        boolean isValid = (start != -1) && (end != -1);

        return  isValid ? json.substring(start + 2, end - 1) : defaultResult;
    }
}
