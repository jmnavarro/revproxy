package com.revproxy.service;

import com.revproxy.model.loadbalancers.AbstractLoadBalancer;
import com.revproxy.model.loadbalancers.RandomLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
@Slf4j
public class LoadBalancerRegistryImpl implements LoadBalancerRegistry {

    private final Map<String, Class<? extends AbstractLoadBalancer>> strategies;

    public LoadBalancerRegistryImpl() {
        strategies = new HashMap<>();
        strategies.put(RandomLoadBalancer.NAME, RandomLoadBalancer.class);
    }

    @Override
    public Optional<AbstractLoadBalancer> createLoadBalancer(@NonNull String loadBalancerName) {
        var klass = strategies.get(loadBalancerName);
        try {
            AbstractLoadBalancer instance = klass.getDeclaredConstructor().newInstance();
            return Optional.of(instance);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }

}
