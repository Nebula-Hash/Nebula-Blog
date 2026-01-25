package com.nebula.tasks;

import com.nebula.upload.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 临时文件清理定时任务
 * 定期清理上传后未被正式使用的临时文件
 *
 * @author Nebula-Hash
 * @date 2026/1/24
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.task.temp-clean.enabled", havingValue = "true", matchIfMissing = true)
public class TempFileCleanTask {

    private final FileUploadUtil fileUploadUtil;

    /**
     * 临时文件过期时间（小时）
     */
    private static final int EXPIRED_HOURS = 24;

    /**
     * 每小时执行一次，清理超过24小时的临时文件
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredTempFiles() {
        log.info("开始执行临时文件清理任务...");
        try {
            int cleanedCount = fileUploadUtil.cleanExpiredTempFiles(EXPIRED_HOURS);
            log.info("临时文件清理任务完成，共清理 {} 个文件", cleanedCount);
        } catch (Exception e) {
            log.error("临时文件清理任务执行失败：{}", e.getMessage(), e);
        }
    }
}
