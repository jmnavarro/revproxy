package com.revproxy.service;

import com.insightfullogic.lambdabehave.JunitSuiteRunner;
import com.revproxy.model.ProxyRule;
import com.revproxy.model.loadbalancers.RandomLoadBalancer;
import com.revproxy.model.loadbalancers.RoundRobinLoadBalancer;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.insightfullogic.lambdabehave.Suite.describe;

@RunWith(JunitSuiteRunner.class)
public class LoadBalancerRegistryImplSpecTest {{

    describe("When all load balancers are registered", it -> {

        var loadBalancerRegistry = new LoadBalancerRegistryImpl();

        it.should("create `random` load balancer", expect -> {
            var loadBalancer = loadBalancerRegistry.createLoadBalancer("random");
            expect.that(loadBalancer.isPresent()).is(true);
            expect.that(loadBalancer.orElse(null)).isNotNull();
            expect.that(loadBalancer.orElse(null)).instanceOf(RandomLoadBalancer.class);
        });

        it.should("create `round-robin` load balancer", expect -> {
            var loadBalancer = loadBalancerRegistry.createLoadBalancer("round-robin");
            expect.that(loadBalancer.isPresent()).is(true);
            expect.that(loadBalancer.orElse(null)).isNotNull();
            expect.that(loadBalancer.orElse(null)).instanceOf(RoundRobinLoadBalancer.class);
        });

        it.should("return empty for unsupported load balancer name", expect -> {
            var loadBalancer = loadBalancerRegistry.createLoadBalancer("this-is-unsupported");
            expect.that(loadBalancer.isPresent()).is(false);
        });

        it.should("return empty for null load balancer name", expect -> {
            var loadBalancer = loadBalancerRegistry.createLoadBalancer(null);
            expect.that(loadBalancer.isPresent()).is(false);
        });

        it.should("return empty for empty load balancer name", expect -> {
            var loadBalancer = loadBalancerRegistry.createLoadBalancer("");
            expect.that(loadBalancer.isPresent()).is(false);
        });

    });
}}
