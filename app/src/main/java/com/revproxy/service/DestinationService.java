package com.revproxy.service;

import com.revproxy.model.ProxyDestination;

import java.util.Optional;

public interface DestinationService {
    Optional<ProxyDestination> getDestination(String host);
}
