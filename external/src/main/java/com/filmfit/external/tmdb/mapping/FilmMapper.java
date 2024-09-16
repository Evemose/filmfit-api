package com.filmfit.external.tmdb.mapping;

import com.filmfit.core.common.Country;
import com.filmfit.core.common.Language;
import com.filmfit.core.film.Film;
import com.filmfit.external.tmdb.dto.CollectionInfo;
import com.filmfit.external.tmdb.dto.CountryInfo;
import com.filmfit.external.tmdb.dto.FilmDetails;
import com.filmfit.external.tmdb.dto.SpokenLanguage;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;

@Mapper(componentModel = "spring")
public abstract class FilmMapper {

    @SuppressWarnings("all")
    @Autowired
    protected LanguageMapper languageMapper;

    @SuppressWarnings("all")
    @Autowired
    protected CompanyMapper companyMapper;

    @SuppressWarnings("all")
    @Autowired
    protected CountryMapper countryMapper;

    @SuppressWarnings("all")
    @Autowired
    protected CollectionMapper collectionMapper;

    @Mapping(target = "productionCompanies", expression =
        "java(filmDetails.productionCompanies().stream().map(companyMapper::toCompany).toList())")
    @Mapping(target = "productionCountries", expression =
        "java(filmDetails.productionCountries().stream().map(prodCountry -> countryMapper.toCountry(context.countries().get(prodCountry.iso_3166_1()))).toList())")
    @Mapping(target = "spokenLanguages", expression =
        "java(filmDetails.spokenLanguages().stream().map(lang -> languageMapper.toLanguage(context.languages().get(lang.iso_639_1()))).toList())")
    @Mapping(target = "collection",
        expression = "java(context.collection() == null ? null : collectionMapper.toCollection(context.collection()))")
    @Mapping(target = "originalLanguage", expression = "java(mapLanguage(filmDetails, context))")
    @Mapping(target = "originCountry", expression = "java(mapCountry(filmDetails, context))")
    @Mapping(target = "tmdbId", expression = "java(filmDetails.id())")
    @Mapping(target = "releaseDate", expression = "java(filmDetails.releaseDate().isBlank() ? null : java.time.LocalDate.parse(filmDetails.releaseDate()))")
    public abstract Film toFilm(@NonNull @Validated FilmDetails filmDetails, FilmMappingContext context);

    @SuppressWarnings("unused")
    protected Language mapLanguage(FilmDetails filmDetails, FilmMappingContext context) {
        if (filmDetails.originalLanguage().equals(Language.NONE.getIso6391())) {
            return Language.NONE;
        }
        return languageMapper.toLanguage(context.languages().get(filmDetails.originalLanguage()));
    }

    @SuppressWarnings("unused")
    protected List<Country> mapCountry(FilmDetails filmDetails, FilmMappingContext context) {
        return filmDetails.productionCountries().stream()
            .map(country -> countryMapper.toCountry(context.countries().get(country.iso_3166_1())))
            .toList();
    }

    public record FilmMappingContext(
        Map<String, SpokenLanguage> languages,
        Map<String, CountryInfo> countries,
        CollectionInfo collection
    ) {
    }
}
