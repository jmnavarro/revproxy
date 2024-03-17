package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class ProxySenderImpl implements ProxySender {
    
    public ProxySenderImpl() {
    }

    @Override
    public ProxyResponse send(@NonNull ProxyDestination destination, @NonNull ProxyRequest request) {
        //TODO
        return null;
    }

}
