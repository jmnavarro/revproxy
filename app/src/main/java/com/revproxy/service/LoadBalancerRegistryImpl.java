package com.revproxy.service;

import com.revproxy.model.loadbalancers.AbstractLoadBalancer;
import com.revproxy.model.loadbalancers.RandomLoadBalancer;
import com.revproxy.model.loadbalancers.RoundRobinLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
@Slf4j
public class LoadBalancerRegistryImpl implements LoadBalancerRegistry {

    private final Map<String, Class<? extends AbstractLoadBalancer>> strategies;

    public LoadBalancerRegistryImpl() {
        strategies = new HashMap<>();
        strategies.put(RandomLoadBalancer.NAME, RandomLoadBalancer.class);
        strategies.put(RoundRobinLoadBalancer.NAME, RoundRobinLoadBalancer.class);
    }

    @Override
    public Optional<AbstractLoadBalancer> createLoadBalancer(@NonNull String loadBalancerName) {
        if (!StringUtils.hasLength(loadBalancerName)) {
            return Optional.empty();
        }

        var klass = strategies.get(loadBalancerName);
        if (klass == null) {
            return Optional.empty();
        }

        try {
            AbstractLoadBalancer instance = klass.getDeclaredConstructor().newInstance();
            return Optional.of(instance);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return Optional.empty();
        }
    }

}
