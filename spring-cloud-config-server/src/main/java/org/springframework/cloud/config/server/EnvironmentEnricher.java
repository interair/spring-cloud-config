package org.springframework.cloud.config.server;

import org.springframework.cloud.config.environment.Environment;

public interface EnvironmentEnricher {

    void processEnvironment(Environment environment, String[] searchLocations);
}
