package com.filmfit.actualization.films;

import com.filmfit.core.film.FilmService;
import com.filmfit.core.film.FilmsInitializationFinishedEvent;
import com.filmfit.external.ReactiveFilmProvider;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitialFilmPopulator {

    private final FilmService filmService;

    private final ReactiveFilmProvider reactiveFilmProvider;

    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener
    public void populate(ApplicationStartedEvent ignored) {
        var start = System.currentTimeMillis();
        var counter = new AtomicInteger();


        if (filmService.isInitialized()) {
            log.info("Films already populated. Skipping");
            return;
        } else {
            log.info("Starting films population");
        }

        var logger = startRegularLogging(counter);

        reactiveFilmProvider.getAllFilms()
            .doOnNext(film -> {
                filmService.saveDeferred(film);
                counter.incrementAndGet();
            })
            .doOnComplete(() -> applicationEventPublisher.publishEvent(new FilmsInitializationFinishedEvent()))
            .doOnComplete(() -> log.info("Films population finised in {} seconds", (System.currentTimeMillis() - start) / 1000f))
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
                    log.error("Logging thread interrupted", e);
                    return;
                }
                log.info("Populated {} films, total {}", counter.get() - current, counter.get());
                current = counter.get();
            }
        });
    }

}
