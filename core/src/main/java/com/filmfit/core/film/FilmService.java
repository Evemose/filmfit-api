package com.filmfit.core.film;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;

    private final TransactionTemplate transactionTemplate;

    @Getter
    private boolean initialized = false;

    private final BlockingQueue<Film> saveQueue = new LinkedBlockingQueue<>();

    private final int saveBatchSize = 100;

    @EventListener
    public void onInitializationFinished(FilmsInitializationFinishedEvent ignored) {
        initialized = true;
    }

    @Async
    public void saveDeferred(Film film) {
        saveQueue.add(film);
        if (saveQueue.size() >= saveBatchSize) {
            saveBatch();
        }
    }

    private void saveBatch() {
        transactionTemplate.executeWithoutResult(_ -> {
            var batch = new LinkedBlockingQueue<Film>(saveBatchSize);
            saveQueue.drainTo(batch, saveBatchSize);
            filmRepository.saveAll(batch);
        });
    }

    @PostConstruct
    public void checkIfInitialized() {
        initialized = filmRepository.count() > 0;
    }

}
