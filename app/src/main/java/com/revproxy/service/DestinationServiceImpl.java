package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService{

    @NonNull
    private final Map<String, ProxyDestination> destinations = new HashMap<>();

    public DestinationServiceImpl() {
        this.load();
    }

    private void load() {
        //TODO
    }

    
    @Override
    public Optional<ProxyDestination> getDestination(String host) {
        return Optional.ofNullable(destinations.get(host));
    }
}
