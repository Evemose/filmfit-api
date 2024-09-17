package com.filmfit.core.film;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import lombok.Getter;
import lombok.Locked;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
class FilmServiceImpl implements FilmService {

    private static final int SAVE_BATCH_SIZE = 100;

    private final FilmRepository filmRepository;
    private final TransactionTemplate transactionTemplate;

    private final BlockingQueue<Film> saveQueue = new LinkedBlockingQueue<>();
    @Getter
    private boolean initialized = false;

    FilmServiceImpl(
        FilmRepository filmRepository,
        PlatformTransactionManager transactionManager
    ) {
        this.filmRepository = filmRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setIsolationLevel(TransactionTemplate.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
    }

    @EventListener
    protected void onInitializationFinished(FilmsInitializationFinishedEvent ignored) {
        initialized = true;
    }

    @Async
    @Override
    public void saveDeferred(Film film) {
        saveQueue.add(film);
        if (saveQueue.size() >= SAVE_BATCH_SIZE) {
            saveBatch(SAVE_BATCH_SIZE);
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

    @Override
    public void flushDeferred() {
        saveBatch(saveQueue.size());
    }

    @Override
    public Film save(Film film) {
        return filmRepository.save(film);
    }

    @PostConstruct
    public void checkIfInitialized() {
        initialized = filmRepository.count() > 0;
    }

}
