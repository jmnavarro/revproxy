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
import java.util.Map;
import java.util.Optional;

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
                                            @NonNull @RequestHeader Map<String, String> headers,
                                            @RequestBody(required = false) Object body) {
        // There's some literature around the semantics of the body in GET requests.
        // Supporting body in this context doesn't do any harm, while it makes the server more
        // flexible, so it seems to be a good idea to support it.
        return ReactiveRequestContextHolder.getRequest()
                .map(ServerHttpRequest::getURI)
                .map(path -> new ProxyRequest(HttpMethod.GET, path.getScheme(), path.getPath(), params, headers, Optional.of(body)))
                .flatMap(proxyService::send)
                .map(this::processResult);
    }

    private ResponseEntity<Object> processResult(@NonNull ProxyResponse result) {
        return ResponseEntity.status(result.status())
                .headers(headers ->  Optional.of(result.headers()).ifPresent(resultHeaders -> resultHeaders.forEach(headers::set)))
                .body(result.body());
    }

}
