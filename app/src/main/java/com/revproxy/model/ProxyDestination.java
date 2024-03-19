package com.revproxy.model;

import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Builder
public record ProxyDestination(
        @NonNull
        String from,

        @NonNull
        String to,

        @NonNull
        String loadBalancerName,

        @Nullable
        Map<String, String> additionalHeaders,

        @Nullable Map<String, String> additionalParams) {

}
