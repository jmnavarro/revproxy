package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class LoadBalancerServiceImpl implements LoadBalancerService {

    @Override
    public ProxyDestination chooseDestination(List<ProxyDestination> destinations) {
        return destinations.getFirst();
    }
}
