package com.filmfit.core.film;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import lombok.Locked;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;

@Service
public class FilmService {

    private final FilmRepository filmRepository;

    private final TransactionTemplate transactionTemplate;

    private final ReactiveFilmRepository reactiveFilmRepository;
    private final BlockingQueue<Film> saveQueue = new LinkedBlockingQueue<>();
    private final int saveBatchSize = 100;
    @Getter
    private boolean initialized = false;

    FilmService(
        FilmRepository filmRepository,
        PlatformTransactionManager transactionManager,
        ReactiveFilmRepository reactiveFilmRepository
    ) {
        this.filmRepository = filmRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.reactiveFilmRepository = reactiveFilmRepository;
        transactionTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    @EventListener
    public void onInitializationFinished(FilmsInitializationFinishedEvent ignored) {
        initialized = true;
    }

    @Async
    public void saveDeferred(Film film) {
        saveQueue.add(film);
        if (saveQueue.size() >= saveBatchSize) {
            saveBatch(saveBatchSize);
        }
    }

    @Locked
    private void saveBatch(int size) {
        transactionTemplate.executeWithoutResult(_ -> {
            var batch = new LinkedBlockingQueue<Film>(size);
            saveQueue.drainTo(batch, size);
            filmRepository.saveAll(batch);
        });
    }

    public void flushDeferred() {
        saveBatch(saveQueue.size());
    }

    public Film save(Film film) {
        return filmRepository.save(film);
    }

    public Mono<Film> saveReactive(Film film) {
        return reactiveFilmRepository.save(film);
    }

    @PostConstruct
    public void checkIfInitialized() {
        initialized = filmRepository.count() > 0;
    }

}
