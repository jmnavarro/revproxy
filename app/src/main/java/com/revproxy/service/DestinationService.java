package com.revproxy.service;

import com.revproxy.model.ProxyDestination;

import java.util.List;

public interface DestinationService {
    List<ProxyDestination> getDestinations(String host);
}
