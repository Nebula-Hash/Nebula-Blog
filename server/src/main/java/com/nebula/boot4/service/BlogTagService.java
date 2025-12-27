package com.nebula.boot4.service;

import com.nebula.boot4.dto.TagDTO;
import com.nebula.boot4.vo.TagVO;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
public interface BlogTagService {

    /**
     * 创建标签
     *
     * @param tagDTO 标签信息
     * @return 标签ID
     */
    Long createTag(TagDTO tagDTO);

    /**
     * 更新标签
     *
     * @param tagDTO 标签信息
     */
    void updateTag(TagDTO tagDTO);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<TagVO> getAllTags();
}
