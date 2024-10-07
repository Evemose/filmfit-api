package com.filmfit.core.film;

import java.util.List;

/**
 * Service for querying films. Methods defined here will throw {@link IllegalStateException}
 * if database is not yet populated.
 */
public interface QueryFilmService {

    boolean isInitialized();

    List<Film> findKMostSimilar(Film film, int k);

}
