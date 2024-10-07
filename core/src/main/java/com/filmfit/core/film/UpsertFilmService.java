package com.filmfit.core.film;

import org.springframework.scheduling.annotation.Async;

public interface UpsertFilmService {
    @Async
    void saveDeferred(Film film);

    void flushDeferred();

    Film save(Film film);
}
