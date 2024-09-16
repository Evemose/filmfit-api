package com.filmfit.core.common;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// TODO: add id before db repopulation
public class Country {

    @Id
    private String iso_3166_1;

    @Column(unique = true, updatable = false)
    private String nativeName;

    @Column(unique = true, updatable = false)
    private String englishName;
}
