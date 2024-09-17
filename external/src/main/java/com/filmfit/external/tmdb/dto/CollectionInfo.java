package com.filmfit.external.tmdb.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CollectionInfo(
    Boolean adult,
    String backdropPath,
    Long id,
    String name,
    String originalLanguage,
    String originalName,
    String overview,
    String posterPath
) {
}
