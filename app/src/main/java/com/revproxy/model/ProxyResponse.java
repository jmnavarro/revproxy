package com.revproxy.model;

import lombok.Builder;
import org.springframework.lang.NonNull;

import java.util.Map;

@Builder
public record ProxyResponse(int status, Object body, @NonNull Map<String, String> headers) {
    
}
