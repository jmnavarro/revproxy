package com.revproxy.service;

import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ProxyService {
    Mono<ProxyResponse> send(@NonNull ProxyRequest request);
}