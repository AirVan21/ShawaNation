package ru.spbau.shawanation.ner;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * LocationRecognizer class - is a class for extracting patterns from text
 */
public class LocationRecognizer {
    private final static String SEARCH_TAG = "LOCATION";
    private final static String PATH_TO_CLASSIFIER = "./libs/english.all.3class.distsim.crf.ser.gz";
    private final AbstractSequenceClassifier<CoreLabel> classifier;

    public LocationRecognizer() throws IOException, ClassNotFoundException {
        this.classifier = CRFClassifier.getClassifier(PATH_TO_CLASSIFIER);
    }

    public List<String> getLocations(String sentence) {
        final List<Triple<String, Integer, Integer>> tagsFromSentence = classifier.classifyToCharacterOffsets(sentence);

        return tagsFromSentence.stream()
                .filter(description -> description.first.equals(SEARCH_TAG))
                .map(item -> sentence.substring(item.second, item.third))
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
    }
}
