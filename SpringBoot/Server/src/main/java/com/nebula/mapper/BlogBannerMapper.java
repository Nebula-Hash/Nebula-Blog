package com.nebula.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nebula.entity.BlogBanner;
import com.nebula.vo.admin.BannerAdminVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 轮播图Mapper接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@Mapper
public interface BlogBannerMapper extends BaseMapper<BlogBanner> {

    /**
     * 查询轮播图列表（包含文章标题）
     *
     * @return 轮播图列表
     */
    @Select("SELECT b.*, a.title as article_title " +
            "FROM blog_banner b " +
            "LEFT JOIN blog_article a ON b.article_id = a.id " +
            "WHERE b.deleted = 0 " +
            "ORDER BY b.sort ASC, b.create_time DESC")
    List<BannerAdminVO> selectBannerListWithArticle();

    /**
     * 根据ID查询轮播图详情（包含文章标题）
     *
     * @param id 轮播图ID
     * @return 轮播图详情
     */
    @Select("SELECT b.*, a.title as article_title " +
            "FROM blog_banner b " +
            "LEFT JOIN blog_article a ON b.article_id = a.id " +
            "WHERE b.id = #{id} AND b.deleted = 0")
    BannerAdminVO selectBannerDetailWithArticle(Long id);
}
