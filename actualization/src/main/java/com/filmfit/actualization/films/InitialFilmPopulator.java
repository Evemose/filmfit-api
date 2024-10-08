package com.filmfit.actualization.films;

import com.filmfit.core.film.FilmService;
import com.filmfit.core.film.FilmsInitializationFinishedEvent;
import com.filmfit.external.ReactiveFilmProvider;
import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


// FIXME see where some films disappear
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class InitialFilmPopulator {

    private static final String BACKUP_FILE = "backup_lfs.sql";

    private final FilmService filmService;

    private final ReactiveFilmProvider reactiveFilmProvider;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final JdbcTemplate jdbcTemplate;

    @EventListener
    @SneakyThrows
    public void populate(ApplicationStartedEvent ignored) {
        var start = System.currentTimeMillis();

        var counter = new AtomicInteger();


        if (filmService.isInitialized()) {
            log.info("Films already populated. Skipping");
            return;
        } else {
            log.info("Starting films population");
        }

        var backup = new ClassPathResource(BACKUP_FILE);

        if (backup.exists()) {
            log.info("Found backup file, restoring from it");
            var tables = List.of("film", "genre", "country", "company", "language");
            for (var table : tables) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table + " RESTART IDENTITY CASCADE");
            }
            var script = new String(backup.getInputStream().readAllBytes());
            jdbcTemplate.execute(script);
            log.info("Films population finished in {} seconds", (System.currentTimeMillis() - start) / 1000f);
            applicationEventPublisher.publishEvent(new FilmsInitializationFinishedEvent());
            return;
        }

        var logger = startRegularLogging(counter);

        reactiveFilmProvider.getAllFilms()
            .doOnNext(film -> {
                counter.incrementAndGet();
                Thread.ofVirtual().start(() -> filmService.save(film));
            })
            .doOnComplete(() -> applicationEventPublisher.publishEvent(new FilmsInitializationFinishedEvent()))
            .doOnComplete(() -> log.info("Films population finished in {} seconds", (System.currentTimeMillis() - start) / 1000f))
            .doFinally(_ -> logger.interrupt())
            .doOnError(e -> log.error("Error while populating films", e))
            .subscribe();
    }

    private Thread startRegularLogging(AtomicInteger counter) {
        var minute = 1000 * 60;
        return Thread.ofVirtual().start(() -> {
            var current = 0;
            while (true) {
                try {
                    Thread.sleep(minute);
                } catch (InterruptedException e) {
                    log.info("Population logging thread interrupted");
                    return;
                }
                log.info("Populated {} films, total {}", counter.get() - current, counter.get());
                current = counter.get();
            }
        });
    }

}
