package com.revproxy.model;

import java.util.List;

public abstract class LoadBalancingAbstractStrategy {
    public abstract ProxyDestination chooseDestination(List<ProxyDestination> destinations);
}
