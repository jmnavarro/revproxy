package com.revproxy.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

public record ProxyDestination(@NonNull String from, @NonNull String to, @Nullable Map<String, String> additionalHeaders, @Nullable Map<String, String> additionalParams) {
    
}