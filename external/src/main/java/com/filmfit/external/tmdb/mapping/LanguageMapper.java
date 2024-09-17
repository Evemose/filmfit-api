package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.common.Language;
import com.filmfit.external.tmdb.dto.SpokenLanguage;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LanguageMapper {

    @Mapping(target = "iso6391", source = "iso_639_1")
    Language toLanguage(@NonNull SpokenLanguage languageDetails);
}
