package com.filmfit.core.film.vector;

import com.filmfit.core.film.Film;
import com.filmfit.core.repos.CountryRepository;
import com.filmfit.core.repos.GenreRepository;
import com.filmfit.core.repos.LanguageRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class EncodedFilmService {

    private final EncodedFilmRepository encodedFilmRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final CountryRepository countryRepository;
    private final TransactionTemplate transactionTemplate;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Initializes the encoded film repository for the given source of films.
     *
     * @param films the source of films, supplier must be re-usable,
     *              as the films will be read multiple times
     */
    @SneakyThrows
    public void initializeForSource(Supplier<Stream<Film>> films) {
        log.info("Initializing encoded films");
        encodedFilmRepository.deleteAll();

        var vectorizersAndNormalizers = buildVectorizersAndNormalizers(films);
        processFilms(films, vectorizersAndNormalizers);
    }

    private VectorizersAndNormalizers buildVectorizersAndNormalizers(Supplier<Stream<Film>> films) {
        return transactionTemplate.execute(_ -> {
            var overviewVectorizerBuilder = new TFIDFCounter.Builder();
            var titleVectorizerBuilder = new TFIDFCounter.Builder();
            var popularityNormalizerBuilder = new Normalizer.Builder();
            var voteAverageNormalizerBuilder = new Normalizer.Builder();
            var voteCountNormalizerBuilder = new Normalizer.Builder();
            var revenueNormalizerBuilder = new Normalizer.Builder();
            var budgetNormalizerBuilder = new Normalizer.Builder();
            var runtimeNormalizerBuilder = new Normalizer.Builder();

            try (var stream = films.get()) {
                stream.parallel().forEach(film -> {
                    overviewVectorizerBuilder.consume(film.getOverview());
                    titleVectorizerBuilder.consume(film.getTitle());

                    popularityNormalizerBuilder.consume(film.getPopularity());
                    voteAverageNormalizerBuilder.consume(film.getVoteAverage());
                    voteCountNormalizerBuilder.consume(film.getVoteCount());
                    revenueNormalizerBuilder.consume(film.getRevenue());
                    budgetNormalizerBuilder.consume(film.getBudget());
                    runtimeNormalizerBuilder.consume(film.getRuntime());
                });
            }

            return new VectorizersAndNormalizers(
                overviewVectorizerBuilder.build(),
                titleVectorizerBuilder.build(),
                popularityNormalizerBuilder.build(),
                voteAverageNormalizerBuilder.build(),
                voteCountNormalizerBuilder.build(),
                revenueNormalizerBuilder.build(),
                budgetNormalizerBuilder.build(),
                runtimeNormalizerBuilder.build()
            );
        });
    }

    private void processFilms(Supplier<Stream<Film>> films, VectorizersAndNormalizers vectorizersAndNormalizers) {
        jdbcTemplate.execute("alter table encoded_film alter column title_vector type vector(%d)".formatted(vectorizersAndNormalizers.titleVectorizer().totalTerms()));
        jdbcTemplate.execute("alter table encoded_film alter column overview_vector type vector(%d)".formatted(vectorizersAndNormalizers.overviewVectorizer().totalTerms()));
        transactionTemplate.executeWithoutResult(_ -> {
            var genreMaxId = ((int) genreRepository.findMaxId());
            var languageMaxId = ((int) languageRepository.findMaxId());
            var countryMaxId = ((int) countryRepository.findMaxId());

            try (var stream = films.get()) {
                stream.forEach(film -> {
                        var titleVector = vectorizersAndNormalizers.titleVectorizer().getTFIDF(film.getTitle()).stream().mapToDouble(Double::doubleValue).toArray();
                        var overviewVector = vectorizersAndNormalizers.overviewVectorizer().getTFIDF(film.getOverview()).stream().mapToDouble(Double::doubleValue).toArray();

                        var popularity = vectorizersAndNormalizers.popularityNormalizer().normalize(film.getPopularity());
                        var voteAverage = vectorizersAndNormalizers.voteAverageNormalizer().normalize(film.getVoteAverage());
                        var voteCount = vectorizersAndNormalizers.voteCountNormalizer().normalize(film.getVoteCount());
                        var revenue = vectorizersAndNormalizers.revenueNormalizer().normalize(film.getRevenue());
                        var budget = vectorizersAndNormalizers.budgetNormalizer().normalize(film.getBudget());
                        var runtime = vectorizersAndNormalizers.runtimeNormalizer().normalize(film.getRuntime());

                        var genreFlags = film.getGenres().stream().map(genre -> genre.getId().intValue() - 1).collect(toOneHot(genreMaxId));
                        var languageFlags = film.getSpokenLanguages().stream().map(language -> language.getId().intValue() - 1).collect(toOneHot(languageMaxId));
                        var countryFlags = film.getProductionCountries().stream().map(country -> country.getId().intValue() - 1).collect(toOneHot(countryMaxId));
                        var originalLanguage = encodeOriginalLanguage(film, languageMaxId);
                        var adult = new byte[] {(byte) (film.getAdult() ? 1 : 0)};

                        var encodedFilm = new EncodedFilm(
                            null,
                            film,
                            titleVector,
                            overviewVector,
                            popularity,
                            voteAverage,
                            voteCount,
                            revenue,
                            budget,
                            runtime,
                            genreFlags,
                            countryFlags,
                            languageFlags,
                            originalLanguage,
                            adult,
                            film.getProductionCompanies().stream().toList(),
                            film.getCollection()
                        );

                        encodedFilmRepository.save(encodedFilm);
                    });
            }
        });
    }

    private static byte[] encodeOriginalLanguage(Film film, int maxId) {
        var originalLanguage = film.getOriginalLanguage();
        var arr = new byte[maxId];
        if (originalLanguage != null) {
            arr[originalLanguage.getId().intValue() - 1] = 1;
        }
        return arr;
    }

    private Collector<Integer, ?, byte[]> toOneHot(int maxId) {
        return Collectors.collectingAndThen(
            Collectors.toList(),
            list -> {
                var oneHot = new byte[maxId];
                list.forEach(id -> oneHot[id] = 1);
                return oneHot;
            }
        );
    }

    private float[] toFloatArray(double[] values) {
        var floatArray = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            floatArray[i] = (float) values[i];
        }
        return floatArray;
    }

    public List<Long> findIdsOfKMostSimilar(Film film, int k) {
        return encodedFilmRepository.findKMostSimilar(film.getId(), k).stream()
            .map(EncodedFilm::getFilm)
            .map(Film::getId)
            .toList();
    }

    private record VectorizersAndNormalizers(
        TFIDFCounter overviewVectorizer,
        TFIDFCounter titleVectorizer,
        Normalizer popularityNormalizer,
        Normalizer voteAverageNormalizer,
        Normalizer voteCountNormalizer,
        Normalizer revenueNormalizer,
        Normalizer budgetNormalizer,
        Normalizer runtimeNormalizer
    ) {
    }
}

