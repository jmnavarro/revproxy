package com.revproxy.service;

import com.revproxy.model.LoadBalancingAbstractStrategy;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface LoadBalancerRegistry {

    Optional<LoadBalancingAbstractStrategy> createLoadBalancer(@NonNull String loadBalancerName);

}
