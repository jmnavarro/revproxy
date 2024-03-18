package com.revproxy.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.revproxy.model.ProxyDestination;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

@Service
@Slf4j
public class DestinationServiceImpl implements DestinationService{

    @NonNull
    private final ResourceLoader resourceLoader;
    @NonNull
    private final Map<String, ProxyDestination> destinations = new HashMap<>();

    public DestinationServiceImpl(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.load();
    }

    private void load() {
        Optional.of(resourceLoader.getResource("classpath:destinations.json"))
                .map(DestinationServiceImpl::getResourceAsString)
                .ifPresentOrElse(fileData -> {
                    final var typeToken = new TypeToken<List<ProxyDestination>>() {};
                    final List<ProxyDestination> list = new Gson().fromJson(fileData, typeToken.getType());
                    list.forEach(d -> destinations.put(d.from(), d));
                    log.info("Destinations loaded");
                }, () -> log.warn("No destinations file found"));
    }


    @Override
    public Optional<ProxyDestination> getDestination(String origin) {
        return Optional.ofNullable(destinations.get(origin));
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
