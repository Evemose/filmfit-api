package com.filmfit.core.common;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language {

    @Transient
    public static final Language NONE = new Language("xx", "No Language", "No Language");

    @Id
    private String iso6391;
    private String name;
    private String englishName;
}
