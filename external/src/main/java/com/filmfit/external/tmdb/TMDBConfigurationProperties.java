package com.filmfit.external.tmdb;

import java.util.Collections;
import java.util.Map;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@ConfigurationProperties("tmdb")
class TMDBConfigurationProperties {

    @NonNull
    public final String baseUrl;

    @NonNull
    private final Map<Endpoint, String> endpoints;

    @NonNull
    public final String apiKey;

    @ConstructorBinding
    public TMDBConfigurationProperties(@NonNull String baseUrl, @NonNull Map<Endpoint, String> endpoints, @NonNull String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;

        for (var endpoint : Endpoint.values()) {
            if (!endpoints.containsKey(endpoint)) {
                throw new IllegalArgumentException("Missing endpoint: " + endpoint);
            }
        }

        this.endpoints = Collections.unmodifiableMap(endpoints);
    }

    public String getEndpoint(@NonNull Endpoint endpoint) {
        return endpoints.get(endpoint);
    }

}
