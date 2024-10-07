package com.filmfit.core.repos;

import com.filmfit.core.common.Country;
import java.util.Optional;

public interface CountryRepository extends MaxIdRepository<Country> {

    Optional<Country> findByEnglishName(String englishName);

}
