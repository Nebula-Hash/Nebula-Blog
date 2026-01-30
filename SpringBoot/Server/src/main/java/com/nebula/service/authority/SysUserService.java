package com.nebula.service.authority;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nebula.dto.UserDTO;
import com.nebula.entity.SysUser;
import com.nebula.vo.UserAdminVO;

/**
 * 用户服务接口
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 按角色分页查询用户
     *
     * @param current 当前页
     * @param size    每页大小
     * @param roleKey 角色标识
     * @return 分页结果
     */
    Page<UserAdminVO> pageUsersByRole(Long current, Long size, String roleKey);

    /**
     * 根据ID查询用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    UserAdminVO getUserById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    SysUser getUserByEmail(String email);

    /**
     * 新增用户
     *
     * @param userDTO 用户信息
     * @param roleKey 角色标识
     * @return 是否成功
     */
    boolean addUser(UserDTO userDTO, String roleKey);

    /**
     * 更新用户
     *
     * @param userDTO 用户信息
     * @return 是否成功
     */
    boolean updateUser(UserDTO userDTO);

    /**
     * 删除用户
     *
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);

    /**
     * 搜索用户（多字段模糊匹配）
     *
     * @param current 当前页
     * @param size    每页大小
     * @param keyword 搜索关键词（匹配用户名、昵称）
     * @param status  用户状态（可选）
     * @return 分页结果
     */
    Page<UserAdminVO> searchUsers(Long current, Long size, String keyword, Integer status);
}
