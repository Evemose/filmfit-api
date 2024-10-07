package com.filmfit.core.film.vector;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
class TFIDFCounter {

    private static final Set<String> stopWords = loadStopWords();
    @Getter(lazy = true, value = AccessLevel.PRIVATE)
    private static final StanfordCoreNLP pipeline = producePipeline();

    private final Map<String, Long> termFrequency;
    private final long documentCount;

    @SneakyThrows
    private static Set<String> loadStopWords() {
        try (var input = Objects.requireNonNull(TFIDFCounter.class.getClassLoader().getResourceAsStream("nlp/stopwords-en.txt"));
             var reader = new BufferedReader(new InputStreamReader(input))) {
            return reader.lines().collect(Collectors.toSet());
        }
    }

    private static StanfordCoreNLP producePipeline() {
        return new StanfordCoreNLP(new Properties() {{
            setProperty("annotators", "tokenize,ssplit,pos,lemma");
            setProperty("tokenize.language", "en");
        }});
    }

    private static List<String> getTerms(String document) {
        var pipeline = getPipeline();
        var annotation = pipeline.processToCoreDocument(document);
        return annotation.tokens().stream()
            .filter(token -> !stopWords.contains(token.lemma().toLowerCase()))
            .map(token -> token.lemma().toLowerCase())
            .toList();
    }

    public List<Double> getTFIDF(String document) {
        var terms = getTerms(document);
        var docTermFrequency = new HashMap<String, Long>();
        for (var term : terms) {
            docTermFrequency.merge(term, 1L, Long::sum);
        }
        var allTerms = termFrequency.keySet();

        return allTerms.stream()
            .map(term -> docTermFrequency.getOrDefault(term, 0L) *
                Math.log((double) documentCount / termFrequency.computeIfAbsent(term, t -> {
                    log.warn("Term {} not found in the corpus, but present in the document {}", t, document);
                    return 1L;
                })))
            .toList();
    }

    public int totalTerms() {
        return termFrequency.size();
    }

    static class Builder {
        private final Map<String, Long> termFrequency = new ConcurrentHashMap<>();
        private final AtomicLong documentCount = new AtomicLong(0);

        public void consume(String document) {
            documentCount.incrementAndGet();
            var terms = getTerms(document);
            terms.forEach(term -> termFrequency.merge(term, 1L, Long::sum));
        }

        public TFIDFCounter build() {
            return new TFIDFCounter(termFrequency, documentCount.get());
        }
    }
}
