package com.revproxy.model.loadbalancers;

import com.revproxy.model.ProxyDestination;
import com.revproxy.model.loadbalancers.AbstractLoadBalancer;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "random";

    @Override
    public ProxyDestination selectDestination(List<ProxyDestination> destinations) {
        Random rand = new Random();
        int randomNum = rand.nextInt(destinations.size());
        return destinations.get(randomNum);
    }
}
