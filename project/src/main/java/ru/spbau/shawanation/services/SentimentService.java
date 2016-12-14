package ru.spbau.shawanation.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

@Service
public class SentimentService {
    private final Logger logger = Logger.getLogger(SentimentService.class.getName());

    public double calcSentiment(String text) {
        try {
            final File tempFile = new File("../sentiment_analysis/Docker/Volume", "input_file.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            bw.write(text);
            bw.close();
            return callSentimentService(tempFile.getName(), "outputFile.txt");
        } catch (IOException e) {
            logger.warning(e.toString());
            return -1;
        }
    }

    private double callSentimentService(String inputFile, String outputFile) {
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.getForObject("http://127.0.0.1:5001/sentiment/" + inputFile + "/" + outputFile, String.class);
        return Double.parseDouble(result);
    }
}
