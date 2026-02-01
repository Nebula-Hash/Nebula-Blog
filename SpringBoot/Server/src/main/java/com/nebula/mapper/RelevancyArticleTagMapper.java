package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.RelevancyArticleTag;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章标签关联Mapper接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Mapper
public interface RelevancyArticleTagMapper extends BaseMapper<RelevancyArticleTag> {

    /**
     * 批量插入文章标签关联
     *
     * @param list 文章标签关联列表
     * @return 插入行数
     */
    @Insert("<script>" +
            "INSERT INTO relevancy_article_tag (article_id, tag_id, create_time) VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.articleId}, #{item.tagId}, NOW())" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("list") List<RelevancyArticleTag> list);
}
