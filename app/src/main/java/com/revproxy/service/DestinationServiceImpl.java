package com.revproxy.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService{

    private static final String PROPS_ENV = "REVPROXY_PROPERTIES";
    private static final String RULES_FILE = "classpath:rules.json";

    @NonNull
    private final Map<String, List<ProxyDestination>> destinations;

    public DestinationServiceImpl(@NonNull ResourceLoader resourceLoader, @NonNull LoadBalancerRegistry loadBalancerRegistry) {
        var rulesFileName = loadRulesFileName(resourceLoader);
        String rulesFile = rulesFileName.orElse(RULES_FILE);

        log.info(String.format("Rules file used: %s", rulesFile));

        this.destinations = Optional.of(resourceLoader.getResource(rulesFile))
                .map(DestinationServiceImpl::getResourceAsString)
                .map(fileData -> {
                    final var typeToken = new TypeToken<List<ProxyRule>>() {};
                    final List<ProxyRule> origins = new Gson().fromJson(fileData, typeToken.getType());
                    return origins
                            .stream()
                            .map(rule -> {
                                loadBalancerRegistry
                                        .createLoadBalancer(rule.getLoadBalancerName())
                                        .ifPresent(rule::setLoadBalancer);
                                return rule;
                            })
                            .map(ProxyRule::getDestinations)
                            .flatMap(Collection::stream)
                            .collect(Collectors.groupingBy(ProxyDestination::from));
                }).orElse(Collections.emptyMap());

        if (this.destinations.isEmpty()) {
            log.warn(String.format("[%s] No rules found in the configuration file", rulesFile));
        } else {
            log.info(String.format("[%s] %d rules found in the configuration file", rulesFile, destinations.size()));
        }
    }

    @Override
    public List<ProxyDestination> getDestinations(String origin) {
        var match = destinations.get(extractHost(origin));
        return (match != null) ? match : Collections.emptyList();
    }

    private static String getResourceAsString(@NonNull Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private static String extractHost(String hostWithPort) {
        final var parts = hostWithPort.split(":");
        return (parts.length > 0) ? parts[0] : hostWithPort;
    }

    private String getPropertiesFile() {
        var propsFileEnv = System.getenv(PROPS_ENV);
        var propsFile = propsFileEnv == null ? "classpath:application.properties" : propsFileEnv;

        log.info(String.format("Properties file used: %s", propsFile));

        return propsFile;
    }

    private Optional<String> loadRulesFileName(@NonNull ResourceLoader resourceLoader) {
        try {
            var propsFile = getPropertiesFile();
            var res = resourceLoader.getResource(propsFile);
            Properties p = PropertiesLoaderUtils.loadProperties(res);
            var obj = p.get("revproxy.rules-file");
            return obj == null ? Optional.empty() : Optional.of(obj.toString());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

}
