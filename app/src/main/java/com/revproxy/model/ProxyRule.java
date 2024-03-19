package com.revproxy.model;

import com.google.gson.annotations.SerializedName;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

public record ProxyRule(
        @NonNull
        String from,
        @NonNull
        List<String> to,
        @NonNull
        @SerializedName("load-balancing")
        String loadBalancing,
        @Nullable
        Map<String, String> additionalHeaders,
        @Nullable
        Map<String, String> additionalParams) {

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
