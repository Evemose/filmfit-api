package com.filmfit.core.common;

import com.filmfit.core.repos.CountryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CountryService {

    private final CountryRepository countryRepository;

    public Optional<Country> findByCountryName(String countryName) {
        return countryRepository.findByEnglishName(countryName);
    }

}
