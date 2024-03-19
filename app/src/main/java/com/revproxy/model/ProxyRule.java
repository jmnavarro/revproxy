package com.revproxy.model;

import com.google.gson.annotations.SerializedName;
import com.revproxy.model.loadbalancers.AbstractLoadBalancer;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ProxyRule {

    private static final int DEFAULT_TIMEOUT_SECONDS = 5;

    @NonNull
    String from;

    @NonNull
    List<String> to;

    @NonNull
    @SerializedName("load-balancing")
    String loadBalancerName;

    @Nullable
    AbstractLoadBalancer loadBalancer;

    @Nullable
    Map<String, String> additionalHeaders;

    @Nullable
    Map<String, String> additionalParams;

    @Nullable
    Integer timeout;  // in seconds

    @Nullable
    @SerializedName("max-retries")
    Integer maxRetries;

    public List<ProxyDestination> getDestinations() {
        return this.to.stream().map(
                toUrl -> ProxyDestination.builder()
                        .from(this.from)
                        .to(toUrl)
                        .loadBalancerName(this.loadBalancerName)
                        .loadBalancer(this.loadBalancer)
                        .additionalHeaders(this.additionalHeaders)
                        .additionalParams(this.additionalParams)
                        .timeout(this.timeout == null ? DEFAULT_TIMEOUT_SECONDS : this.timeout)
                        .maxRetries(this.maxRetries == null ? 0 : this.maxRetries)
                        .build()
                ).toList();
    }
}
