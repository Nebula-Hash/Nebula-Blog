package com.nebula.config.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
public class StartupInfoLogger {

    @Value("${server.port:8080}")
    private int port;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${springdoc.swagger-ui.path:/swagger-ui.html}")
    private String swaggerUiPath;

    @Value("${springdoc.api-docs.path:/v3/api-docs}")
    private String apiDocsPath;

    @Value("${springdoc.swagger-ui.enabled:true}")
    private boolean swaggerUiEnabled;

    @Value("${springdoc.api-docs.enabled:true}")
    private boolean apiDocsEnabled;

    @EventListener(ApplicationReadyEvent.class)
    public void logDocsUrl() {
        String baseUrl = "http://localhost:" + port + normalizeContextPath(contextPath);
        if (swaggerUiEnabled) {
            log.info("Swagger UI: {}{}", baseUrl, normalizePath(swaggerUiPath));
        }
        if (apiDocsEnabled) {
            log.info("OpenAPI JSON: {}{}", baseUrl, normalizePath(apiDocsPath));
        }
    }

    private String normalizeContextPath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private String normalizePath(String path) {
        if (path == null || path.isBlank()) {
            return "";
        }
        return path.startsWith("/") ? path : "/" + path;
    }
}
