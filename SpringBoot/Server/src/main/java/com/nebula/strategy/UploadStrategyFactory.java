package com.nebula.strategy;

import com.nebula.properties.UploadProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 上传策略工厂
 *
 * @author Nebula-Hash
 * @date 2025/11/27
 */
@Component
@RequiredArgsConstructor
public class UploadStrategyFactory {

    private final Map<String, UploadStrategy> uploadStrategyMap;
    private final UploadProperties uploadProperties;

    /**
     * 获取上传策略
     *
     * @return 上传策略
     */
    public UploadStrategy getStrategy() {
        String mode = uploadProperties.getMode();
        String beanName = mode + "UploadStrategyImpl";
        return uploadStrategyMap.get(beanName);
    }
}
