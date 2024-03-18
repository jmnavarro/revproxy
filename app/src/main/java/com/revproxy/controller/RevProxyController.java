package com.revproxy.controller;

import com.revproxy.context.ReactiveRequestContextHolder;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import com.revproxy.service.ProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("**")
@RequiredArgsConstructor
@Slf4j
public class RevProxyController {

    @NonNull
    private final ProxyService proxyService;

    @GetMapping
    public Mono<ResponseEntity<Object>> get(@NonNull @RequestParam Map<String, String> params,
                                            @NonNull @RequestHeader Map<String, String> headers) {
        // There's some literature around the semantics of the body in GET requests.
        // Supporting body in this context doesn't do any harm, while it makes the server more
        // flexible, so it seems to be a good idea to support it.
        return handleRequest(HttpMethod.GET, params, headers, Optional.empty());
    }

    @PostMapping
    public Mono<ResponseEntity<Object>> post(@NonNull @RequestParam Map<String, String> params,
                                             @NonNull @RequestHeader Map<String, String> headers,
                                             @RequestBody(required = false) Object body) {
        return handleRequest(HttpMethod.POST, params, headers, Optional.ofNullable(body));
    }

    @PutMapping
    public Mono<ResponseEntity<Object>> put(@NonNull @RequestParam Map<String, String> params,
                                            @NonNull @RequestHeader Map<String, String> headers,
                                            @RequestBody(required = false) Object body) {
        return handleRequest(HttpMethod.PUT, params, headers, Optional.ofNullable(body));
    }

    @PatchMapping
    public Mono<ResponseEntity<Object>> patch(@NonNull @RequestParam Map<String, String> params,
                                              @NonNull @RequestHeader Map<String, String> headers,
                                              @RequestBody(required = false) Object body) {
        return handleRequest(HttpMethod.PATCH, params, headers, Optional.ofNullable(body));
    }

    @DeleteMapping
    public Mono<ResponseEntity<Object>> delete(@NonNull @RequestParam Map<String, String> params,
                                               @NonNull @RequestHeader Map<String, String> headers,
                                               @RequestBody(required = false) Object body) {
        return handleRequest(HttpMethod.DELETE, params, headers, Optional.ofNullable(body));
    }

    @RequestMapping(method=RequestMethod.OPTIONS)
    public Mono<ResponseEntity<Object>> options(@NonNull @RequestParam Map<String, String> params,
                                                @NonNull @RequestHeader Map<String, String> headers) {
        return handleRequest(HttpMethod.OPTIONS, params, headers, Optional.empty());
    }

    @RequestMapping(method=RequestMethod.HEAD)
    public Mono<ResponseEntity<Object>> head(@NonNull @RequestParam Map<String, String> params,
                                             @NonNull @RequestHeader Map<String, String> headers) {
        return handleRequest(HttpMethod.HEAD, params, headers, Optional.empty());
    }

    private Mono<ResponseEntity<Object>> handleRequest(@NonNull HttpMethod method,
                                                       @NonNull Map<String, String> params,
                                                       @NonNull Map<String, String> headers,
                                                       Optional<Object> body) {
        // standarize header keys
        final var lowerCasedHeaders = toLowercaseHeaders(headers);

        return ReactiveRequestContextHolder.getRequest()
                .map(ServerHttpRequest::getURI)
                .map(path -> new ProxyRequest(method, path.getScheme(), path.getPath(), params, lowerCasedHeaders, body))
                .flatMap(proxyService::send)
                .map(this::processResult);
    }

    private Map<String, String> toLowercaseHeaders(Map<String, String> headers) {
        return headers.entrySet().stream().map(
                entry -> new AbstractMap.SimpleEntry<>(entry.getKey().toLowerCase(), entry.getValue())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private ResponseEntity<Object> processResult(@NonNull ProxyResponse result) {
        return ResponseEntity.status(result.status())
                .headers(headers ->  Optional.of(result.headers()).ifPresent(resultHeaders -> resultHeaders.forEach(headers::set)))
                .body(result.body());
    }

}
