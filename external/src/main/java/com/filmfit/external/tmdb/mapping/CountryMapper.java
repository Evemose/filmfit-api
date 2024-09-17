package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.common.Country;
import com.filmfit.external.tmdb.dto.CountryInfo;
import lombok.NonNull;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CountryMapper {

    Country toCountry(@NonNull CountryInfo productionCountry);
}
