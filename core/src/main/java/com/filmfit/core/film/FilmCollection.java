package com.filmfit.core.film;

import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FilmCollection {

    @Id
    private Long id;

    private Long tmdbId;

    @NotBlank
    private String name;

    @NotNull
    private boolean adult;

    private String posterPath;

    private String backdropPath;

    private String originalLanguage;

    private String originalName;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @OneToMany(mappedBy = "collection")
    private List<Film> films = new ArrayList<>();
}
