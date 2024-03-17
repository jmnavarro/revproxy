package com.revproxy.service;

import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRequest;
import com.revproxy.model.ProxyResponse;
import org.springframework.lang.NonNull;

public interface ProxySender {
        public ProxyResponse send(@NonNull ProxyDestination destination, @NonNull ProxyRequest request);
}
