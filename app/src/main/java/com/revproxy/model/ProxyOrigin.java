package com.revproxy.model;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record ProxyOrigin(@NonNull String from, @NonNull List<String> to, @NonNull String loadBalancing, @Nullable Map<String, String> additionalHeaders, @Nullable Map<String, String> additionalParams) {

    public List<ProxyDestination> getDestinations() {
        return this.to.stream().map(
                toUrl -> ProxyDestination.builder()
                        .from(this.from())
                        .to(toUrl)
                        .loadBalancing(this.loadBalancing())
                        .additionalHeaders(this.additionalHeaders())
                        .additionalParams(this.additionalParams())
                        .build()
                ).toList();
    }

}
