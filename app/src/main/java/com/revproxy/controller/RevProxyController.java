package com.revproxy.controller;

import com.revproxy.model.ProxyRequest;
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
        getPath(getURL(request))
                .map(path -> new ProxyRequest(HttpMethod.GET, request.getScheme(), path, params, headers, Optional.of(body)))
                .flatMap(proxyService::send);
        String message = "Hello, World!";
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @NonNull
    private String getURL(@NonNull HttpServletRequest request){
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .build()
                .toUriString();
    }

    private Optional<String> getPath(@NonNull String url) {
        try {
            return Optional.ofNullable(new URI(url).getPath());
        } catch (URISyntaxException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return Optional.empty();
    }


}
