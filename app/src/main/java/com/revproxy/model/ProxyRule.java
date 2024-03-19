package com.revproxy.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ProxyRule {
    @NonNull
    String from;

    @NonNull
    List<String> to;

    @NonNull
    @SerializedName("load-balancing")
    String loadBalancerName;

    @Nullable
    LoadBalancingAbstractStrategy loadBalancer;

    @Nullable
    Map<String, String> additionalHeaders;

    @Nullable
    Map<String, String> additionalParams;

    public List<ProxyDestination> getDestinations() {
        return this.to.stream().map(
                toUrl -> ProxyDestination.builder()
                        .from(this.from)
                        .to(toUrl)
                        .loadBalancerName(this.loadBalancerName)
                        .loadBalancer(this.loadBalancer)
                        .additionalHeaders(this.additionalHeaders)
                        .additionalParams(this.additionalParams)
                        .build()
                ).toList();
    }
}
