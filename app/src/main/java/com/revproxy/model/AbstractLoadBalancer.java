package com.revproxy.model;

import java.util.List;

public abstract class AbstractLoadBalancer {
    public abstract ProxyDestination chooseDestination(List<ProxyDestination> destinations);
}
