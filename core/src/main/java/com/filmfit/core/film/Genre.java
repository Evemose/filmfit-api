package com.filmfit.core.film;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Setter(AccessLevel.NONE)
public class Genre {

    @Id
    @NonNull
    private Long id;

    @Column(unique = true, updatable = false)
    @NotBlank
    @NonNull
    private String name;
}
