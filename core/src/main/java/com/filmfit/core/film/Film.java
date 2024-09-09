package com.filmfit.core.film;

import com.filmfit.core.common.Company;
import com.filmfit.core.common.Country;
import com.filmfit.core.common.Language;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Film {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long tmdbId;

    @NotNull
    private Boolean adult;

    @Column(columnDefinition="TEXT")
    private String backdropPath;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "collection_id")
    private FilmCollection collection;

    @Min(0)
    private Long budget;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<Genre> genres;

    @Column(columnDefinition="TEXT")
    private String homepage;

    @Column(columnDefinition="TEXT")
    private String imdbId;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "movie_countries",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "country_id")
    )
    private List<Country> originCountry;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private Language originalLanguage;

    @Column(columnDefinition="TEXT")
    private String originalTitle;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Min(0)
    private Double popularity;

    @Column(columnDefinition="TEXT")
    private String posterPath;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "movie_production_companies",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "company_id")
    )
    private List<Company> productionCompanies;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "movie_production_countries",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "country_id")
    )
    private List<Country> productionCountries;

    private LocalDate releaseDate;

    @Positive
    private Long revenue;

    @Positive
    private Integer runtime;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "movie_spoken_languages",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "language_id")
    )
    private List<Language> spokenLanguages;

    @Column(columnDefinition="TEXT")
    private String status;

    @Column(columnDefinition="TEXT")
    private String tagline;

    @NotBlank
    @Column(columnDefinition="TEXT")
    private String title;

    private Boolean video;

    @Min(0)
    @Max(10)
    private Double voteAverage;

    @Min(0)
    private Long voteCount;
}
