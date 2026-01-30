package com.nebula.service.auxiliary;

import com.nebula.dto.TagDTO;
import com.nebula.vo.TagAdminVO;
import com.nebula.vo.TagClientVO;

import java.util.List;

/**
 * 标签服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
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
     * 获取所有标签（管理端）
     *
     * @return 标签列表
     */
    List<TagAdminVO> getAllTagsForAdmin();

    /**
     * 获取所有标签（客户端）
     *
     * @return 标签列表
     */
    List<TagClientVO> getAllTagsForClient();

    /**
     * 根据ID获取标签详情（客户端）
     *
     * @param id 标签ID
     * @return 标签详情
     */
    TagClientVO getTagById(Long id);
}
