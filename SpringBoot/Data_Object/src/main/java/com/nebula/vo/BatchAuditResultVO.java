package com.nebula.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量审核结果VO
 *
 * @author Nebula-Hash
 * @date 2026/2/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchAuditResultVO {

    /**
     * 成功审核数量
     */
    private Integer successCount;

    /**
     * 跳过数量（非待审核状态）
     */
    private Integer skippedCount;

    /**
     * 总请求数量
     */
    private Integer totalCount;

    /**
     * 操作描述
     */
    private String message;

    /**
     * 构建成功结果
     */
    public static BatchAuditResultVO success(int successCount, int skippedCount, int totalCount, String message) {
        return new BatchAuditResultVO(successCount, skippedCount, totalCount, message);
    }
}
