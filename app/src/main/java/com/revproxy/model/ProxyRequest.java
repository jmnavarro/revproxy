package com.revproxy.model;

import lombok.Builder;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;

import java.util.Map;
import java.util.Optional;

@Builder
public record ProxyRequest(@NonNull HttpMethod method, @NonNull String scheme, @NonNull String path, @NonNull Map<String, String> params, @NonNull Map<String, String> headers, Optional<Object> body) {

    public String getHost() {
        return headers.get("host");
    }

}
