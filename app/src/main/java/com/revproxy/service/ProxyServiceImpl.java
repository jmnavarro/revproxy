package com.revproxy.service;

import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProxyServiceImpl implements ProxyService {

    @NonNull
    private final DestinationService destinationService;

    @NonNull
    private final ProxySender sender;

    @Override
    public Optional<ProxyResponse> send(@NonNull ProxyRequest request) {
        return Optional.ofNullable(request.headers().get("host"))
                        .flatMap(destinationService::getDestination)
                        .map(destination -> sender.send(destination, request));
    }
}
