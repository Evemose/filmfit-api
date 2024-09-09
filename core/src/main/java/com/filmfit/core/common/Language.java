package com.filmfit.core.common;

import jakarta.persistence.Transient;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language {

    @Id
    private String iso6391;

    private String name;

    private String englishName;

    @Transient
    public static final Language NONE = new Language("xx", "No Language", "No Language");
}
