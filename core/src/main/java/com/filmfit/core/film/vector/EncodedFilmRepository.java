package com.filmfit.core.film.vector;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface EncodedFilmRepository extends JpaRepository<EncodedFilm, Long> {

    @Query(value = """
                   WITH target AS (
                       SELECT
                           id,
                           title_vector,
                           overview_vector,
                           popularity,
                           vote_average,
                           vote_count,
                           revenue,
                           budget,
                           runtime,
                           genre_flags,
                           country_flags,
                           language_flags,
                           adult
                       FROM
                           encoded_film
                       WHERE
                           film_id = :filmId
                   )
                   SELECT
                       ef.*,
                       (1 - (1.0 * (1 - (
                           0.4 * (target.title_vector <-> ef.title_vector) +
                           0.3 * (target.overview_Vector <-> ef.overview_Vector) +
                           0.1 * (target.popularity - ef.popularity) * (target.popularity - ef.popularity) +
                           0.05 * (target.vote_average - ef.vote_average) * (target.vote_average - ef.vote_average) +
                           0.05 * (target.vote_count - ef.vote_count) * (target.vote_count - ef.vote_count) +
                           0.05 * (target.revenue - ef.revenue) * (target.revenue - ef.revenue) +
                           0.025 * (target.budget - ef.budget) * (target.budget - ef.budget) +
                           0.025 * (target.runtime - ef.runtime) * (target.runtime - ef.runtime) +
                           0.01 * hamming_distance(target.genre_flags, ef.genre_flags) +
                           0.01 * hamming_distance(target.country_flags, ef.country_flags) +
                           0.01 * hamming_distance(target.language_flags, ef.language_flags) +
                           0.01 * hamming_distance(target.adult, ef.adult)
                       )) / (0.4 + 0.3 + 0.1 + 0.05 + 0.05 + 0.05 + 0.025 + 0.025 + 0.01 + 0.01 + 0.01 + 0.01))) as combined_similarity
                   FROM
                       encoded_film ef,
                       target
                   WHERE
                       ef.id <> target.id
                   ORDER BY
                       combined_similarity DESC
                   LIMIT :k
                   """, nativeQuery = true)
    List<EncodedFilm> findKMostSimilar(long filmId, int k);

}
