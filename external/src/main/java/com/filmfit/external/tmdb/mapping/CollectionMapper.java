package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.film.FilmCollection;
import com.filmfit.external.tmdb.dto.CollectionInfo;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    @Mapping(target = "films", ignore = true)
    @Mapping(target = "tmdbId", source = "id")
    FilmCollection toCollection(@NonNull CollectionInfo collection);

}
