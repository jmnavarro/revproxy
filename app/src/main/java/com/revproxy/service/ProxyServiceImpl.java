package com.revproxy.service;

import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    @NonNull
    private final DestinationService destinationService;

    @NonNull
    private final LoadBalancerRegistry loadBalancerRegistry;

    @NonNull
    private final ProxySender sender;

    @Override
    public Mono<ProxyResponse> send(@NonNull ProxyRequest request) {
        return Optional.ofNullable(request.headers().get("host"))
                        .map(destinationService::getDestinations)
                        .map(destinations -> {
                            // all load balancer assigned to all destinations are actually references to the same
                            // we can safely return the first one
                            return destinations.getFirst().loadBalancer().selectDestination(destinations);
                        })
                        .map(destination -> sender.send(destination, request))
                        .orElse(Mono.just(
                                    ProxyResponse.builder()
                                        .status(HttpStatus.NOT_FOUND.value())
                                        .body(HttpStatus.NOT_FOUND.getReasonPhrase())
                                        .build()));
    }
}
