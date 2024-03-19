package com.revproxy.service;

import com.revproxy.model.LoadBalancingAbstractStrategy;
import com.revproxy.model.ProxyDestination;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface LoadBalancerService {

    Optional<LoadBalancingAbstractStrategy> createLoadBalancer(@NonNull String loadBalancerName);

}
