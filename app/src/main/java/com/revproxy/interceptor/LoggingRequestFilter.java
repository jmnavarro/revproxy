package com.revproxy.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component
@Slf4j
public class LoggingRequestFilter implements WebFilter {

    private static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private static final String START_REQUEST_FORMAT = "Request: %s, [%s %s]";
    private static final String END_REQUEST_FORMAT = "Request %s completed (%d ms)";
    private static final String ERROR_REQUEST_FORMAT = "Request %s failed with status %d (%d ms)";

    public static void printLog(String str, boolean error) {
        String msg = String.format("[%s] %s", DATE_FORMATER.format(new Date(Instant.now().toEpochMilli())), str);
        if (error) {
            log.error(msg);
        } else {
            log.info(msg);
        }
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        final long startTime = Instant.now().toEpochMilli();
        final var request = exchange.getRequest();
        printLog(String.format(START_REQUEST_FORMAT, request.getId(), request.getMethod(), request.getURI()), false);
        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    final long endTime = Instant.now().toEpochMilli();
                    final long duration = endTime - startTime;
                    final var status = exchange.getResponse().getStatusCode();
                    if (status != null && status.is2xxSuccessful()) {
                        printLog(String.format(END_REQUEST_FORMAT, request.getId(), duration), false);
                    } else {
                        printLog(String.format(ERROR_REQUEST_FORMAT, request.getId(), status.value(), duration), true);
                    }
                })
                .doOnError(error -> {
                    final long endTime = Instant.now().toEpochMilli();
                    final long duration = endTime - startTime;
                    printLog(String.format(ERROR_REQUEST_FORMAT, request.getId(), duration), true);
                });
    }
}
