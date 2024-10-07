package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.common.Company;
import com.filmfit.core.common.CountryService;
import com.filmfit.external.tmdb.dto.ProductionCompany;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class CompanyMapper {

    @SuppressWarnings("all")
    @Autowired
    protected CountryService countryService;

    @Mapping(target = "originCountry", expression = "java(countryService.findByCountryName(company.originCountry()).orElse(null))")
    public abstract Company toCompany(@NonNull ProductionCompany company);

}
