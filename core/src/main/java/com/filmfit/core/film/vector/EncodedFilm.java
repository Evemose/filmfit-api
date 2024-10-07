package com.filmfit.core.film.vector;

import com.filmfit.core.common.Company;
import com.filmfit.core.film.Film;
import com.filmfit.core.film.FilmCollection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class EncodedFilm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the original film
    @OneToOne
    @JoinColumn(unique = true, updatable = false)
    private Film film;

    // Vector features, precise data types are defined in EncodedFilmService during initialization
    @JdbcTypeCode(SqlTypes.VECTOR)
    private double[] titleVector;
    @JdbcTypeCode(SqlTypes.VECTOR)
    private double[] overviewVector;

    // numeric features
    private Double popularity;
    private Double voteAverage;
    private Double voteCount;
    private Double revenue;
    private Double budget;
    private Double runtime;

    // One-hot encoded features
    private byte[] genreFlags;
    private byte[] countryFlags;
    private byte[] spokenLanguageFlags;
    private byte[] originalLanguage;
    private byte[] adult;

    // References for now, potentially denormalized vectors in the future
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private List<Company> productionCompanies;
    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    private FilmCollection collection;
}
