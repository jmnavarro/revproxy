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
    private final ProxySender sender;

    @Override
    public Mono<ProxyResponse> send(@NonNull ProxyRequest request) {
        return Optional.ofNullable(request.headers().get("host"))
                        .flatMap(destinationService::getDestination)
                        .map(destination -> sender.send(destination, request))
                        .orElse(Mono.just(
                                    ProxyResponse.builder()
                                        .status(HttpStatus.NOT_FOUND.value())
                                        .body(HttpStatus.NOT_FOUND.getReasonPhrase())
                                        .build()));
    }
}
