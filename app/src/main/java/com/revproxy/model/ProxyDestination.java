package com.revproxy.model;

import com.revproxy.model.loadbalancers.AbstractLoadBalancer;
import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;

@Builder
public record ProxyDestination(
        @NonNull
        String from,

        @NonNull
        String to,

        @NonNull
        String loadBalancerName,

        @NonNull
        AbstractLoadBalancer loadBalancer,

        @Nullable
        Map<String, String> additionalHeaders,

        @Nullable Map<String, String> additionalParams) {

}
