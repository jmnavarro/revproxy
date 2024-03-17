package com.revproxy.service;

import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface ProxyService {
    Optional<ProxyResponse> send(@NonNull ProxyRequest proxyRequest);
}