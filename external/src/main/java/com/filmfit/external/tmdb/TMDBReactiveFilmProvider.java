package com.filmfit.external.tmdb;

import com.filmfit.core.film.Film;
import com.filmfit.core.film.Genre;
import com.filmfit.external.ReactiveFilmProvider;
import com.filmfit.external.RequestUrlBuilder;
import com.filmfit.external.tmdb.dto.CollectionInfo;
import com.filmfit.external.tmdb.dto.CountryInfo;
import com.filmfit.external.tmdb.dto.FilmDetails;
import com.filmfit.external.tmdb.dto.FilmListItem;
import com.filmfit.external.tmdb.dto.SpokenLanguage;
import com.filmfit.external.tmdb.mapping.FilmMapper;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.retry.Retry;

@Component("TMDBReactiveFilmProvider")
@Slf4j
class TMDBReactiveFilmProvider implements ReactiveFilmProvider {

    private final WebClient webClient;

    private final FilmMapper filmMapper;

    private final TMDBConfigurationProperties properties;

    private static final Integer NO_GENRE = Integer.MIN_VALUE;

    private final RateLimiter rateLimiter = RateLimiter.of("tmdb", RateLimiterConfig.custom()
        .limitRefreshPeriod(Duration.ofSeconds(1))
        .limitForPeriod(40)
        .timeoutDuration(Duration.ofDays(1))
        .build());

    private final Map<Integer, Genre> genres = Map.ofEntries(
        Map.entry(28, new Genre(1L, "Action")),
        Map.entry(12, new Genre(2L, "Adventure")),
        Map.entry(16, new Genre(3L, "Animation")),
        Map.entry(35, new Genre(4L, "Comedy")),
        Map.entry(80, new Genre(5L, "Crime")),
        Map.entry(99, new Genre(6L, "Documentary")),
        Map.entry(18, new Genre(7L, "Drama")),
        Map.entry(10751, new Genre(8L, "Family")),
        Map.entry(14, new Genre(9L, "Fantasy")),
        Map.entry(36, new Genre(10L, "History")),
        Map.entry(27, new Genre(11L, "Horror")),
        Map.entry(10402, new Genre(12L, "Music")),
        Map.entry(9648, new Genre(13L, "Mystery")),
        Map.entry(10749, new Genre(14L, "Romance")),
        Map.entry(878, new Genre(15L, "Science Fiction")),
        Map.entry(10770, new Genre(16L, "TV Movie")),
        Map.entry(53, new Genre(17L, "Thriller")),
        Map.entry(10752, new Genre(18L, "War")),
        Map.entry(37, new Genre(19L, "Western"))
    );

    public TMDBReactiveFilmProvider(WebClient.Builder webClient, TMDBConfigurationProperties properties, FilmMapper filmMapper) {
        var connectionProvider = ConnectionProvider.builder("myConnectionPool")
            .pendingAcquireMaxCount(Integer.MAX_VALUE)
            .build();
        this.webClient = webClient
            .baseUrl(properties.baseUrl)
            .filter(apiKeyRequestParamFilter(properties))
            .filter(logRequest())
            //.clientConnector(new ReactorClientHttpConnector(HttpClient.create(connectionProvider)))
            .build();
        this.properties = properties;
        this.filmMapper = filmMapper;
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.trace("Request: {} {}", clientRequest.method(), clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    private static ExchangeFilterFunction apiKeyRequestParamFilter(TMDBConfigurationProperties properties) {
        return ExchangeFilterFunction.ofRequestProcessor(req -> Mono.just(ClientRequest.from(req).url(
            UriComponentsBuilder.fromUri(req.url().isAbsolute() ? req.url() : URI.create(properties.baseUrl + req.url()))
                .queryParam("api_key", properties.apiKey)
                .build(true)
                .toUri()).build())
        );
    }

    @Override
    public Flux<Film> getAllFilms() {
        return getLanguages()
            .collectMap(SpokenLanguage::iso_639_1, Function.identity())
            .flux()
            .flatMap(language -> getCountries()
                .collectMap(CountryInfo::iso_3166_1, Function.identity())
                .flux()
                .flatMap(country -> getAllFilmsMainPipeline(language, country))
            ).transformDeferred(RateLimiterOperator.of(rateLimiter));
    }

    private Flux<Film> getAllFilmsMainPipeline(Map<String, SpokenLanguage> languages, Map<String, CountryInfo> countries) {
        return getGenres()
            .flatMap(genreAndPreviouslyQueried -> getYears()
                .flatMap(year -> Flux.fromIterable(countries.values())
                    .flatMap(country -> {
                        var queryBuilder = QueryFilmUriOptions.builder()
                            .year(year)
                            .genre(genreAndPreviouslyQueried.getKey())
                            .country(country.iso_3166_1())
                            .previousGenres(genreAndPreviouslyQueried.getValue());

                        return getTotalPages(queryBuilder.build()).flux()
                            .flatMap(totalPages -> {
                                if (totalPages > 1000) {
                                    log.error("Too many pages for genre: {}, year: {}, total pages: {}, country: {}",
                                        genreAndPreviouslyQueried.getKey(), year, totalPages, country.iso_3166_1());
                                    return Flux.error(new RuntimeException("Too many pages"));
                                } else if (totalPages <= 500) {
                                    return Flux.range(1, totalPages)
                                        .flatMap(page -> getAllFilms(queryBuilder.page(page).build()));
                                } else {
                                    return Flux.concat(
                                        Flux.range(1, 500)
                                            .flatMap(page -> getAllFilms(queryBuilder.page(page).build())),
                                        Flux.range(1, totalPages - 500)
                                            .flatMap(page -> getAllFilms(queryBuilder.page(page).ascending(true).build()))
                                    );
                                }
                            }).transformDeferred(RateLimiterOperator.of(rateLimiter));
                    })
                )
                .flatMap(film -> getFilmDetails(film.id()))
                .flatMap(film -> (
                    film.belongsToCollection() != null ? getCollection(film.belongsToCollection().name())
                        : Flux.<CollectionInfo>empty()).zipWith(Mono.just(film))
                )
                .map(collectionsAndFilm -> filmMapper.toFilm(
                    collectionsAndFilm.getT2(),
                    new FilmMapper.FilmMappingContext(languages, countries, collectionsAndFilm.getT1())
                )));
    }

    private Mono<Integer> getTotalPages(QueryFilmUriOptions queryFilmUriOptions) {

        record TotalPagesResponse(int total_pages) {
        }

        return webClient.get()
            .uri(buildGetAllFilmsUri(queryFilmUriOptions))
            .retrieve()
            .bodyToMono(TotalPagesResponse.class)
            .map(resp -> resp.total_pages);
    }

    private Flux<FilmListItem> getAllFilms(QueryFilmUriOptions queryFilmUriOptions) {

        record FilmResponse(List<FilmListItem> results) {
        }

        return webClient.get()
            .uri(buildGetAllFilmsUri(queryFilmUriOptions))
            .retrieve()
            .bodyToMono(FilmResponse.class)
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)).doBeforeRetryAsync(_ -> {
                log.warn("Retrying request, {}", queryFilmUriOptions);
                return Mono.empty();
            }))
            .map(FilmResponse::results)
            .doOnError(e -> log.error("Failed to get films, {}", queryFilmUriOptions, e))
            .doOnNext(films -> log.debug("Received films: {}", films))
            .flux()
            .flatMap(Flux::fromIterable)
            .transformDeferred(RateLimiterOperator.of(rateLimiter)).log();
    }

    private URI buildGetAllFilmsUri(QueryFilmUriOptions queryFilmUriOptions) {
        var builder = RequestUrlBuilder.of(properties.getEndpoint(Endpoint.MOVIES_DISCOVER))
            .requestParam("include_adult", "true")
            .requestParam("year", String.valueOf(queryFilmUriOptions.year()))
            .requestParam("page", String.valueOf(queryFilmUriOptions.page()))
            .requestParam("sort_by", "popularity" + (queryFilmUriOptions.ascending() ? ".asc" : ".desc"))
            .requestParam("with_origin_country", queryFilmUriOptions.country())
            .requestParam("without_genres", queryFilmUriOptions.previousGenres().stream()
                .map(Object::toString).collect(Collectors.joining(",")));

        // there are films with no genre, so we need to include them
        if (queryFilmUriOptions.genre() != NO_GENRE) {
            builder.requestParam("with_genres", String.valueOf(queryFilmUriOptions.genre()));
        }

        return builder.buildUri();
    }

    private Flux<Map.Entry<Integer, List<Integer>>> getGenres() {
        var genresAndPrevious = new ArrayList<Map.Entry<Integer, List<Integer>>>();
        var currentPrevious = new ArrayList<Integer>();

        for (var genre : genres.keySet()) {
            genresAndPrevious.add(Map.entry(genre, List.copyOf(currentPrevious)));
            currentPrevious.add(genre);
        }

        genresAndPrevious.add(Map.entry(NO_GENRE, List.copyOf(currentPrevious)));

        return Flux.fromIterable(genresAndPrevious);
    }

    private Flux<Integer> getYears() {
        return Flux.range(1850, LocalDate.now().getYear() - 1850);
    }

    private Mono<FilmDetails> getFilmDetails(long id) {
        return webClient.get()
            .uri(properties.getEndpoint(Endpoint.MOVIES_DETAILS), id)
            .retrieve()
            .bodyToMono(FilmDetails.class)
            .timeout(Duration.ofMinutes(1))
            .doOnNext(film -> log.debug("Received film details: {}", film))
            .transformDeferred(RateLimiterOperator.of(rateLimiter));
    }

    private Flux<SpokenLanguage> getLanguages() {
        return getFromEndpoint(SpokenLanguage.class, URI.create(properties.getEndpoint(Endpoint.LANGUAGES)));
    }

    private Flux<CountryInfo> getCountries() {
        return getFromEndpoint(CountryInfo.class, URI.create(properties.getEndpoint(Endpoint.COUNTRIES)));
    }

    private Flux<CollectionInfo> getCollection(String name) {
        record CollectionResponse(List<CollectionInfo> results) {
        }

        return getFromEndpoint(CollectionResponse.class, RequestUrlBuilder.of(properties.getEndpoint(Endpoint.COLLECTIONS))
            .requestParam("query", '"' + name + '"')
            .requestParam("include_adult", "true")
            .buildUri())
            .doOnNext(item -> logReceivedItem(CollectionResponse.class, item))
            .flatMap(response -> response.results().isEmpty() ?
                logMissingAndReturnEmpty(CollectionResponse.class, Map.of("query", name, "include_adult", "true"))
                : Flux.just(response.results().getFirst()))
            .transformDeferred(RateLimiterOperator.of(rateLimiter)).log();
    }

    private static <T> void logReceivedItem(Class<T> clazz, T item) {
        log.debug("Received {} item: {}", clazz.getSimpleName(), item);
    }

    private <T> Flux<T> logMissingAndReturnEmpty(Class<?> clazz, Map<String, String> parameters) {
        log.warn("Failed to get {} for parameters {}", clazz.getSimpleName(), parameters);
        return Flux.empty();
    }

    private <T> Flux<T> getFromEndpoint(Class<T> clazz, URI endpoint) {

        return webClient.get()
            .uri(endpoint)
            .retrieve()
            .bodyToFlux(clazz)
            .timeout(Duration.ofMinutes(1))
            .doOnNext(item -> logReceivedItem(clazz, item))
            .transformDeferred(RateLimiterOperator.of(rateLimiter)).log();
    }

    @Builder
    private record QueryFilmUriOptions(int page, int genre, int year, boolean ascending, List<Integer> previousGenres, String country) {
        @SuppressWarnings("all")
        public static class QueryFilmUriOptionsBuilder {
            private int page = 1;
            private boolean ascending = false;
            private List<Integer> previousGenres = List.of();
        }
    }
}
