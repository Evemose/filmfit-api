package com.filmfit.external.tmdb.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.hibernate.validator.constraints.Range;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FilmListItem(
    @NotNull @Positive Long id,
    @NotNull Boolean adult,
    String backdropPath,
    @NotEmpty List<@Positive Integer> genreIds,
    @NotBlank String originalLanguage,
    @NotBlank String originalTitle,
    String overview,
    @Positive Double popularity,
    String posterPath,
    @NotBlank String releaseDate,
    @NotBlank String title,
    @NotNull Boolean video,
    @Range(min = 0, max = 10) Double voteAverage,
    @Positive Long voteCount
) {
}

