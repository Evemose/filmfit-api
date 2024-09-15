package com.filmfit.core.film;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

interface ReactiveFilmRepository extends ReactiveCrudRepository<Film, Long> {
}
