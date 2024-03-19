package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class ProxySenderImpl implements ProxySender {

    private final WebClient webClient;


    public ProxySenderImpl(WebClient.Builder webClientBuilder) {
        super();
        this.webClient = webClientBuilder.build();
    }

    @Override
    public Mono<ProxyResponse> send(@NonNull ProxyDestination destination, @NonNull ProxyRequest request) {
        // Build the URL
        final var url = buildURL(request, destination);
        log.info("Forwarding request to " + url);

        // Send the Request
        final var requestBuilder = webClient.method(request.method())
                .uri(url)
                .headers(headers -> addHeaders(headers, request, destination));
        request.body().ifPresent(requestBuilder::bodyValue);
        return requestBuilder.retrieve()
                .toEntity(Resource.class)
                .map(entity -> ProxyResponse.builder()
                        .status(entity.getStatusCode().value())
                        .headers(entity.getHeaders().toSingleValueMap())
                        .body(entity.getBody())
                        .build());
    }

    @NonNull
    private URI buildURL(@NonNull ProxyRequest request, @NonNull ProxyDestination destination) {
        // Build the URL From the Proxy Destination and Request Path
        final var builder = UriComponentsBuilder.newInstance();
        builder.scheme(getScheme(request, destination));
        builder.host(getHost(destination));
        builder.path(getPath(request, destination));
        // Add Request Query Params
        request.params().forEach(builder::queryParam);
        // Add Destination Query Params, if present
        Optional.ofNullable(destination.additionalParams()).ifPresent(params -> params.forEach(builder::queryParam));
        // Return the URI
        return builder.build().toUri();
    }

    @SuppressWarnings("null")
    @NonNull
    private String getScheme(@NonNull ProxyRequest request, @NonNull ProxyDestination destination) {
        final var destinationURI = URI.create(destination.to());
        final var scheme = destinationURI.getScheme();
        if (scheme != null && !scheme.isEmpty()) {
            return scheme;
        }
        return request.scheme();
    }

    @SuppressWarnings("null")
    @NonNull
    private String getHost(@NonNull ProxyDestination destination) {
        final var host = URI.create(destination.to()).getHost();
        if (host != null && !host.isEmpty()) {
            return host;
        }
        return destination.to();
    }

    @SuppressWarnings("null")
    @NonNull
    private String getPath(@NonNull ProxyRequest request, @NonNull ProxyDestination destination) {
        final var destinationURI = URI.create(destination.to());
        final var path = destinationURI.getPath();
        if (path != null && !path.isEmpty() && !path.equalsIgnoreCase(destination.to())) {
            return path;
        }
        return request.path();
    }

    private void addHeaders(HttpHeaders headers, ProxyRequest request, ProxyDestination destination) {
        addHeaders(headers, request.headers());
        Optional.ofNullable(destination.additionalHeaders()).ifPresent(ad -> addHeaders(headers, ad));
    }

    private void addHeaders(HttpHeaders headers, Map<String, String> headersToAdd) {
        headersToAdd.forEach((key, value) -> {
            if (!key.equalsIgnoreCase("host")) {
                headers.add(key, value);
            }
        });
    }

}
