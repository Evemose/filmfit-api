package com.filmfit.external.tmdb.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.filmfit.core.film.Genre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.NonNull;
import org.hibernate.validator.constraints.Range;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FilmDetails(
    Long id,
    @NotNull @NonNull Boolean adult,
    String backdropPath,
    BelongsToCollection belongsToCollection,
    @Positive Long budget,
    List<Genre> genres,
    String homepage,
    String imdbId,
    @NotEmpty @NonNull List<String> originCountry,
    @NotBlank @NonNull String originalLanguage,
    @NotBlank @NonNull String originalTitle,
    String overview,
    @Positive Double popularity,
    String posterPath,
    List<ProductionCompany> productionCompanies,
    List<ProductionCountry> productionCountries,
    @NotNull @NonNull String releaseDate,
    @Positive Long revenue,
    @Positive Integer runtime,
    List<SpokenLanguage> spokenLanguages,
    String status,
    String tagline,
    @NotBlank @NonNull String title,
    Boolean video,
    @Range(min = 0, max = 10) Double voteAverage,
    @Positive Long voteCount) {

}
