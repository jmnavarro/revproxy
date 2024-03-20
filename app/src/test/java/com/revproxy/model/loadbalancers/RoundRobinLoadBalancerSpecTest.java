package com.revproxy.model.loadbalancers;

import com.insightfullogic.lambdabehave.JunitSuiteRunner;
import com.revproxy.model.ProxyRule;
import org.junit.runner.RunWith;

import java.util.List;

import static com.insightfullogic.lambdabehave.Suite.describe;

@RunWith(JunitSuiteRunner.class)
public class RoundRobinLoadBalancerSpecTest {{

    describe("When the load balancer is created with one destination", it -> {
        var loadBalancer = new RoundRobinLoadBalancer();

        var destinations = ProxyRule.builder()
                .from("host1")
                .to(List.of("downstream_1"))
                .loadBalancerName("round-robin")
                .loadBalancer(loadBalancer)
                .build()
                .getDestinations();

        it.should("returns the same destination over and over again", expect -> {
            for (var i = 0; i < 10; i++) {
                var destination = loadBalancer.selectDestination(destinations);
                expect.that(destination.to()).is("downstream_1");
            }
        });
    });

    describe("When the load balancer is created with two destinations", it -> {
        var loadBalancer = new RoundRobinLoadBalancer();

        var destinations = ProxyRule.builder()
                .from("host1")
                .to(List.of("downstream_1", "downstream_2"))
                .loadBalancerName("round-robin")
                .loadBalancer(loadBalancer)
                .build()
                .getDestinations();

        it.should("rotates the destination after each request", expect -> {
            loadBalancer.setRequestUnits(1);
            loadBalancer.setDestinationTotalUnits(1);

            expect.that(loadBalancer.selectDestination(destinations).to()).is("downstream_1");
            expect.that(loadBalancer.selectDestination(destinations).to()).is("downstream_2");
            expect.that(loadBalancer.selectDestination(destinations).to()).is("downstream_1");
        });
    });

}}
