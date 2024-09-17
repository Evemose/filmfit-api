package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.common.Company;
import com.filmfit.external.tmdb.dto.ProductionCompany;
import lombok.NonNull;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    Company toCompany(@NonNull ProductionCompany company);

}
