package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.common.Country;
import com.filmfit.external.tmdb.dto.CountryInfo;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    Country toCountry(@NonNull CountryInfo productionCountry);
}
