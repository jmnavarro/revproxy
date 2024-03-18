package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class ProxySenderImpl implements ProxySender {

    public ProxySenderImpl() {
        super();
    }

    @Override
    public Mono<ProxyResponse> send(@NonNull ProxyDestination destination, @NonNull ProxyRequest request) {
        //TODO
        return Mono.just(new ProxyResponse(200, null, Map.of()));
    }

}
