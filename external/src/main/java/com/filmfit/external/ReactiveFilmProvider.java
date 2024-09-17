package com.filmfit.external;

import com.filmfit.core.film.Film;
import reactor.core.publisher.Flux;

public interface ReactiveFilmProvider {

    Flux<Film> getAllFilms();

}
