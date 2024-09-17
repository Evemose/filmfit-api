package com.filmfit.core.film;

import org.springframework.data.jpa.repository.JpaRepository;

interface FilmRepository extends JpaRepository<Film, Long> {
}
