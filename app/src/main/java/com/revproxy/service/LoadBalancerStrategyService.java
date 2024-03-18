package com.revproxy.service;

import com.revproxy.model.ProxyDestination;

import java.util.List;

public interface LoadBalancerStrategyService {
    ProxyDestination chooseDestination(List<ProxyDestination> destinations);
}
