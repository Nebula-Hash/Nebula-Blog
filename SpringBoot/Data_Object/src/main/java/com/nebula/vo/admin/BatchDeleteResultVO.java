package com.nebula.vo.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量删除结果VO
 *
 * @author Nebula-Hash
 * @date 2026/2/2
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteResultVO {

    /**
     * 删除的根评论数量
     */
    private Integer deletedRootCount;

    /**
     * 删除的子评论数量
     */
    private Integer deletedChildCount;

    /**
     * 总删除数量
     */
    private Integer totalDeletedCount;

    /**
     * 不存在的评论数量
     */
    private Integer notFoundCount;

    /**
     * 操作描述
     */
    private String message;

    /**
     * 构建成功结果
     */
    public static BatchDeleteResultVO success(int rootCount, int childCount, int notFoundCount, String message) {
        return new BatchDeleteResultVO(rootCount, childCount, rootCount + childCount, notFoundCount, message);
    }
}
