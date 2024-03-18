package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

public interface ProxySender {
        public Mono<ProxyResponse> send(@NonNull ProxyDestination destination, @NonNull ProxyRequest request);
}
