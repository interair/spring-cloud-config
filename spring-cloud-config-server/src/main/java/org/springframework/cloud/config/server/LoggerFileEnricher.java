package org.springframework.cloud.config.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.config.server.NativeEnvironmentRepository.*;

/**
 * Loads content of logger configuration file if 'logging.config' property was specified
 * to PropertySource map with key 'logging.config.src'
 *
 */
public class LoggerFileEnricher implements EnvironmentEnricher {

    private static Log logger = LogFactory.getLog(LoggerFileEnricher.class);
    private static final String LOGGING_CONFIG = "logging.config";
    private static final String LOGGING_CONFIG_SRC = "logging.config.src";

    public void processEnvironment(Environment environment, String[] searchLocations) {
        readLoggerConfigurationFile(environment, searchLocations);
    }

    protected void readLoggerConfigurationFile(Environment result, String[] searchLocations) {
        final List<PropertySource> propertySources = result.getPropertySources();

        for (PropertySource source : propertySources) {

            final String loggingConfig = (String) source.getSource().get(LOGGING_CONFIG);

            if (StringUtils.hasText(loggingConfig)) {
                for (String pattern : getLocations(result.getLabel(), searchLocations)) {

                    File logFile = new File(normalize(pattern), loggingConfig);
                    if (logFile.exists() && !logFile.isDirectory()) {
                        try {
                            updatePropertySource(logFile, source, propertySources);
                            break;
                        } catch (IOException e) {
                            logger.error("Can't read logger configuration file", e);
                        }
                    }
                }
            }
        }
    }



    private void updatePropertySource(File logFile, PropertySource source, List<PropertySource> propertySources)
            throws IOException {

        final String loggerConfigContent = readFile(logFile, Charset.defaultCharset());
        Map<Object, Object> map = new HashMap<>();
        map.putAll(source.getSource());
        map.put(LOGGING_CONFIG_SRC, loggerConfigContent);
        PropertySource propertySource = new PropertySource(source.getName(), map);
        propertySources.remove(source);
        propertySources.add(propertySource);
    }

    private static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
        return new String(encoded, encoding);
    }
}
