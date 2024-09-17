package com.filmfit.external;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestUrlBuilder {

    private final String url;

    private final Map<String, String> pathVariables;

    private final Map<String, String> requestParams = new HashMap<>();

    public static RequestUrlBuilder of(@NonNull String url) {

        if (!url.matches("(/[\\w{}]+)+")) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        var params = extractParameters(url).stream()
            .<Map<String, String>>collect(HashMap::new, (m, v) -> m.put(v, null), Map::putAll);
        return new RequestUrlBuilder(url, params);
    }

    private static List<String> extractParameters(String url) {
        var matcher = Pattern.compile("\\{(\\w+)}").matcher(url);
        return matcher.results()
            .map(m -> m.group(1))
            .toList();
    }

    public RequestUrlBuilder pathVar(@NonNull String key, @NonNull String value) {
        pathVariables.put(key, value);
        return this;
    }

    public RequestUrlBuilder requestParam(@NonNull String key, @NonNull String value) {
        requestParams.put(key, URLEncoder.encode(value, StandardCharsets.UTF_8));
        return this;
    }

    public URI buildUri() {
        return URI.create(build());
    }

    public String build() {
        if (pathVariables.values().stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("Not all path variables are set");
        }

        var builder = new StringBuilder(url);

        if (!requestParams.isEmpty()) {
            builder.append("?");
            for (var entry : requestParams.entrySet()) {
                builder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            builder.deleteCharAt(builder.length() - 1);
        }

        for (var entry : pathVariables.entrySet()) {
            builder.replace(
                builder.indexOf("{" + entry.getKey() + "}"),
                builder.indexOf("{" + entry.getKey() + "}") + entry.getKey().length() + 2,
                entry.getValue()
            );
        }

        return builder.toString();
    }
}
