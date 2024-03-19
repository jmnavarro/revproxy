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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService{

    private static final String RULES_FILE = "rules.json";

    @NonNull
    private final Map<String, List<ProxyDestination>> destinations;

    public DestinationServiceImpl(@NonNull ResourceLoader resourceLoader) {
        this.destinations = Optional.of(resourceLoader.getResource("classpath:" + RULES_FILE))
                .map(DestinationServiceImpl::getResourceAsString)
                .map(fileData -> {
                    final var typeToken = new TypeToken<List<ProxyRule>>() {};
                    final List<ProxyRule> origins = new Gson().fromJson(fileData, typeToken.getType());
                    return origins
                            .stream()
                            .map(ProxyRule::getDestinations)
                            .flatMap(Collection::stream)
                            .collect(Collectors.groupingBy(ProxyDestination::from));
                }).orElse(Collections.emptyMap());

        if (this.destinations.isEmpty()) {
            log.warn("No origins found in the configuration file");
        } else {
            log.info(destinations.size() + " origins found in the configuration file");
        }
    }

    @Override
    public List<ProxyDestination> getDestinations(String origin) {
        final var parts = origin.split(":");
        if (parts.length > 0) {
            return destinations.get(parts[0]);
        }

        return Collections.emptyList();
    }

    private static String getResourceAsString(@NonNull Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

}
