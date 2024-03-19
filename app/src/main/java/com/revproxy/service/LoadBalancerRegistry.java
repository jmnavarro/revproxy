package com.revproxy.service;

import com.revproxy.model.loadbalancers.AbstractLoadBalancer;
import org.springframework.lang.NonNull;

import java.util.Optional;

public interface LoadBalancerRegistry {

    Optional<AbstractLoadBalancer> createLoadBalancer(@NonNull String loadBalancerName);

}
