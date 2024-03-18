package com.revproxy.service;

import com.revproxy.model.LoadBalancingAbstractStrategy;
import com.revproxy.model.LoadBalancingRandomStrategy;
import com.revproxy.model.ProxyDestination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LoadBalancerServiceImpl implements LoadBalancerService {

    private final Map<String, LoadBalancingAbstractStrategy> strategies;

    public LoadBalancerServiceImpl() {
        strategies = new HashMap<>();
        strategies.put(LoadBalancingRandomStrategy.NAME, new LoadBalancingRandomStrategy());
    }

    @Override
    public ProxyDestination chooseDestination(List<ProxyDestination> destinations) {
        var strategyNameToUse = destinations.getFirst().loadBalancing();
        return Optional.ofNullable(strategies.get(strategyNameToUse))
                .map(strategy -> strategy.chooseDestination(destinations))
                .orElse(destinations.getFirst());  // use the first when no strategy
    }
}
