package com.revproxy.model;

import com.insightfullogic.lambdabehave.JunitSuiteRunner;
import com.revproxy.model.loadbalancers.RandomLoadBalancer;
import org.junit.runner.RunWith;

import java.util.List;

import static com.insightfullogic.lambdabehave.Suite.describe;

@RunWith(JunitSuiteRunner.class)
public class ProxyRuleSpecTest {{

    describe("When a ProxyRule is loaded", it -> {

        var rule = ProxyRule.builder()
                .from("host1")
                .to(List.of("downstream_1", "downstream_2"))
                .loadBalancerName("random")
                .loadBalancer(new RandomLoadBalancer())
                .build();

        var destinations = rule.getDestinations();

        it.should("return correct number of ProxyDestination objects", expect -> {
            expect.that(destinations.size()).is(2);
        });

        it.should("return correct ProxyDestination objects", expect -> {
            expect.that(destinations.get(0).to()).is("downstream_1");
            expect.that(destinations.get(1).to()).is("downstream_2");
        });

        it.should("return same load balancers in all ProxyDestination objects", expect -> {
            var loadBalancer = destinations.getFirst().loadBalancer();
            destinations.forEach(d -> {
                expect.that(d.loadBalancer()).sameInstance(loadBalancer);
            });
        });

        it.should("set default timeout in the ProxyDestination objects", expect -> {
            destinations.forEach(d -> {
                expect.that(d.timeout()).is(ProxyRule.DEFAULT_TIMEOUT_SECONDS);
            });
        });

        it.should("set no maxRetries in the ProxyDestination objects", expect -> {
            destinations.forEach(d -> {
                expect.that(d.maxRetries()).is(0);
            });
        });
    });
}}
