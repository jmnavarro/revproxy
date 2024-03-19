package com.revproxy.service;

import com.revproxy.model.LoadBalancingAbstractStrategy;
import com.revproxy.model.LoadBalancingRandomStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
@Slf4j
public class LoadBalancerRegistryImpl implements LoadBalancerRegistry {

    private final Map<String, Class<? extends LoadBalancingAbstractStrategy>> strategies;

    public LoadBalancerRegistryImpl() {
        strategies = new HashMap<>();
        strategies.put(LoadBalancingRandomStrategy.NAME, LoadBalancingRandomStrategy.class);
    }

    @Override
    public Optional<LoadBalancingAbstractStrategy> createLoadBalancer(@NonNull String loadBalancerName) {
        var klass = strategies.get(loadBalancerName);
        try {
            LoadBalancingAbstractStrategy instance = klass.getDeclaredConstructor().newInstance();
            return Optional.of(instance);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }

}
