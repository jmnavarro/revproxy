package com.revproxy.service;

import com.revproxy.interceptor.LoggingRequestFilter;
import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
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

        LoggingRequestFilter.printLog(String.format("Forward %s://%s%s to %s", request.scheme(), request.getHost(), request.path(), url), false);

        // Send the Request
        final var requestBuilder = webClient.method(request.method())
                .uri(url)
                .headers(headers -> addHeaders(headers, request, destination));

        request.body().ifPresent(requestBuilder::bodyValue);

        return requestBuilder.retrieve()
                .toEntity(Resource.class)
                .timeout(Duration.ofSeconds(destination.timeout()))
                .onErrorReturn(new ResponseEntity<>(HttpStatus.GATEWAY_TIMEOUT))
                .retry(destination.maxRetries())
                .map(entity -> ProxyResponse.builder()
                        .status(entity.getStatusCode().value())
                        .headers(entity.getHeaders().toSingleValueMap())
                        .body(entity.getBody())
                        .build());
    }

    @NonNull
    private URI buildURL(@NonNull ProxyRequest request, @NonNull ProxyDestination destination) {
        // Build the URL From the Proxy Destination and Request Path
        final var builder = UriComponentsBuilder.newInstance()
                .scheme(getScheme(request, destination))
                .host(getHost(destination))
                .port(getPort(destination))
                .path(getPath(request, destination));
        // Add Request Query Params
        request.params().forEach(builder::queryParam);
        // Add Destination Query Params, if present
        Optional.ofNullable(destination.additionalParams()).ifPresent(params -> params.forEach(builder::queryParam));
        // Return the URI
        return builder.build().toUri();
    }

    @NonNull
    private String getScheme(@NonNull ProxyRequest request, @NonNull ProxyDestination destination) {
        final var destinationURI = URI.create(destination.to());
        final var scheme = destinationURI.getScheme();
        if (scheme != null && !scheme.isEmpty()) {
            return scheme;
        }
        return request.scheme();
    }

    @NonNull
    private String getHost(@NonNull ProxyDestination destination) {
        final var host = URI.create(destination.to()).getHost();
        if (host != null && !host.isEmpty()) {
            return host;
        }
        return destination.to();
    }

    @NonNull
    private int getPort(@NonNull ProxyDestination destination) {
        final var port = URI.create(destination.to()).getPort();
        return (port == -1) ? 80 : port;
    }

    @NonNull
    private String getPath(@NonNull ProxyRequest request, @NonNull ProxyDestination destination) {
        final var destinationURI = URI.create(destination.to());
        final var path = destinationURI.getPath();
        if (path != null && !path.isEmpty() && !path.equals(destination.to())) {
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
            if (!key.equals("host")) {
                headers.add(key, value);
            }
        });
    }

}
