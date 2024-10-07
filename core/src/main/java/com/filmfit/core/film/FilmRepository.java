package com.filmfit.core.film;

import static org.hibernate.jpa.HibernateHints.*;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

interface FilmRepository extends JpaRepository<Film, Long> {

    @QueryHints({
        @QueryHint(name = HINT_READ_ONLY, value = "true"),
        @QueryHint(name = HINT_FETCH_SIZE, value = "10000"),
        @QueryHint(name = HINT_CACHEABLE, value = "true"),
    })
    @Query(value = "SELECT f FROM Film f")
    Stream<Film> streamAll();

    List<Film> findAllByIdIn(List<Long> ids);
}
