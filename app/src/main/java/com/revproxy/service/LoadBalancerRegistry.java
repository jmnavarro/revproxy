package com.revproxy.service;

import com.revproxy.model.AbstractLoadBalancer;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface LoadBalancerRegistry {

    Optional<AbstractLoadBalancer> createLoadBalancer(@NonNull String loadBalancerName);

}
