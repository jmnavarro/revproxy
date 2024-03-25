package com.revproxy.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revproxy.model.ProxyDestination;
import com.revproxy.model.ProxyRule;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService{

    private static final String PROPS_ENV = "REVPROXY_PROPERTIES";
    private static final String RULES_FILE = "classpath:rules.json";

    @NonNull
    private final Map<String, List<ProxyDestination>> destinations;

    public DestinationServiceImpl(@NonNull ResourceLoader resourceLoader, @NonNull LoadBalancerRegistry loadBalancerRegistry) {
        this.destinations = Optional.of(resourceLoader)
                .flatMap(this::getRulesFileName)
                .flatMap(makeGetRulesFile(resourceLoader))
                .map(DestinationServiceImpl::getFileAsString)
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
            log.warn("No rules found in the configuration file");
        } else {
            log.info(String.format("%d rules found in the configuration file", destinations.size()));
            for (var e : destinations.entrySet()) {
                e.getValue().stream().map(d -> String.format("  - %s\n", d.toString())).forEach(log::info);
            }
        }
    }

    @Override
    public List<ProxyDestination> getDestinations(String origin) {
        var match = destinations.get(extractHost(origin));
        return (match != null) ? match : Collections.emptyList();
    }

    private static String getFileAsString(@NonNull File file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file.getAbsoluteFile().getPath())));
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

    private Optional<String> getRulesFileName(@NonNull ResourceLoader resourceLoader) {
        try {
            var propsFile = getPropertiesFile();
            var res = resourceLoader.getResource(propsFile);
            Properties p = PropertiesLoaderUtils.loadProperties(res);
            var obj = p.get("revproxy.rules-file");
            return Optional.of(obj == null ? RULES_FILE : obj.toString());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
            return Optional.empty();
        }
    }

    private Function<String, Optional<File>> makeGetRulesFile(@NonNull ResourceLoader resourceLoader) {
        return (String rulesFileName) -> {
            if (rulesFileName.startsWith("classpath:")) {
                log.warn(String.format("Using internal Rules file: %s", rulesFileName));

                try {
                    return Optional.of(resourceLoader.getResource(rulesFileName).getFile());
                } catch (IOException e) {
                    log.error(e.getLocalizedMessage(), e);
                    return Optional.empty();
                }
            } else {
                log.info(String.format("External Rules file used: %s", rulesFileName));
                return Optional.of(new File(rulesFileName.replace("file://","").replace("file:","")));
            }
        };
    }
}
