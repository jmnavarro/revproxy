package com.revproxy.model.loadbalancers;

import com.revproxy.model.ProxyDestination;

import java.util.List;

public abstract class AbstractLoadBalancer {
    public abstract ProxyDestination chooseDestination(List<ProxyDestination> destinations);
}
