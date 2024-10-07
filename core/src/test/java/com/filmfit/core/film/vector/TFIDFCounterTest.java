package com.filmfit.core.film.vector;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TFIDFCounterTest {

    @Test
    @DisplayName("Given multiple documents, when configuredForDocuments is called, then it should calculate term frequencies correctly")
    @SuppressWarnings("unchecked")
    void testConfiguredForDocuments() throws Exception {
        var documents = Stream.of(
            "the quick brown fox",
            "the lazy dog",
            "the quick dog jumps"
        );

        var tfidfCounter = TFIDFCounter.configuredForDocuments(documents);
        var termFrequency = (Map<String, Long>) ReflectionUtils.tryToReadFieldValue(TFIDFCounter.class, "termFrequency", tfidfCounter).get();
        var documentCount = (Long) ReflectionUtils.tryToReadFieldValue(TFIDFCounter.class, "documentCount", tfidfCounter).get();

        assertEquals(6, termFrequency.size());
        assertEquals(3, documentCount);

        assertEquals(2, termFrequency.get("quick"));
        assertEquals(1, termFrequency.get("brown"));
        assertEquals(2, termFrequency.get("dog"));
        assertEquals(1, termFrequency.get("jump"));
        assertEquals(1, termFrequency.get("lazy"));
        assertEquals(1, termFrequency.get("fox"));
    }

    @Test
    @DisplayName("Given a document, when getTFIDF is called, then it should calculate the correct TF-IDF values")
    void testGetTFIDF() {
        // Given
        var documents = Stream.of(
            "the quick brown fox",
            "the lazy dog",
            "the quick dog jumps"
        );
        var tfidfCounter = TFIDFCounter.configuredForDocuments(documents);
        var document = "the quick dog";

        var tfidfValues = tfidfCounter.getTFIDF(document);

        assertEquals(2, tfidfValues.size());
        assertEquals(Math.log(1.5), tfidfValues.getFirst()); // TF-IDF for "quick"
        assertEquals(Math.log(1.5), tfidfValues.getLast()); // TF-IDF for "dog"
    }
}
