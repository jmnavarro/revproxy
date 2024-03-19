package com.revproxy.model;

import java.util.List;
import java.util.Random;

public class RandomLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "random";

    @Override
    public ProxyDestination chooseDestination(List<ProxyDestination> destinations) {
        Random rand = new Random();
        int randomNum = rand.nextInt(destinations.size());
        return destinations.get(randomNum);
    }
}
