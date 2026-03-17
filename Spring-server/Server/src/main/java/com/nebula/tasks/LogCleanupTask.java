package com.nebula.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Slf4j
@Component
@Profile("prod")
@ConditionalOnProperty(name = "app.task.log-clean.enabled", havingValue = "true", matchIfMissing = true)
public class LogCleanupTask {

    private final Path logRoot;
    private final int retentionDays;

    public LogCleanupTask(
            @Value("${logging.file.path:.logs}") String logPath,
            @Value("${app.task.log-clean.retention-days:30}") int retentionDays) {
        this.logRoot = Paths.get(logPath).toAbsolutePath().normalize();
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "${app.task.log-clean.cron:0 0 3 * * ?}")
    public void cleanLogs() {
        if (!Files.exists(logRoot)) {
            log.info("Log cleanup skipped, path not found: {}", logRoot);
            return;
        }

        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        AtomicInteger deletedCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        Path sqlRoot = logRoot.resolve("sql").normalize();
        try (Stream<Path> paths = Files.walk(logRoot)) {
            paths.filter(Files::isRegularFile).forEach(path -> {
                if (path.normalize().startsWith(sqlRoot)) {
                    return;
                }
                try {
                    FileTime lastModified = Files.getLastModifiedTime(path);
                    if (lastModified.toInstant().isBefore(cutoff)) {
                        Files.deleteIfExists(path);
                        deletedCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    log.warn("Failed to delete log file: {}", path, e);
                }
            });
        } catch (IOException e) {
            log.error("Log cleanup failed for path: {}", logRoot, e);
            return;
        }

        log.info("Log cleanup finished. deleted={}, errors={}, retentionDays={}",
                deletedCount.get(), errorCount.get(), retentionDays);
    }
}
