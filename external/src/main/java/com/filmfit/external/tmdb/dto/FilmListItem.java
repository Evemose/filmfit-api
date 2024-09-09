package com.filmfit.external.tmdb.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Range;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FilmListItem (
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
) {}

