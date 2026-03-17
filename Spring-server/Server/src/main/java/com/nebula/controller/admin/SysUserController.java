package com.nebula.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.UserDTO;
import com.nebula.entity.SysUser;
import com.nebula.result.Result;
import com.nebula.service.authority.SysUserService;
import com.nebula.vo.admin.UserAdminVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@AdminController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService sysUserService;

    // ==================== 用户搜索 ====================

    /**
     * 搜索用户（多字段模糊匹配，不区分角色）
     *
     * @param current 当前页，默认1
     * @param size    每页大小，默认10
     * @param keyword 搜索关键词（匹配用户名、昵称，必填，最少…2字符）
     * @param status  用户状态（可选，0-禁用 1-启用）
     * @return 用户分页数据
     */
    @GetMapping("/search")
    public Result<Page<UserAdminVO>> searchUsers(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam String keyword,
            @RequestParam(required = false) Integer status) {
        // 关键词校验：必填且最少2个字符
        if (keyword == null || keyword.trim().length() < 2) {
            return Result.error("搜索关键词至少需要2个字符");
        }
        Page<UserAdminVO> page = sysUserService.searchUsers(current, size, keyword.trim(), status);
        return Result.success(page);
    }

    // ==================== 管理员账号管理 ====================

    /**
     * 分页查询管理员列表
     *
     * @param current 当前页，默认1
     * @param size    每页大小，默认10
     * @return 管理员分页数据
     */
    @GetMapping("/admin/page")
    public Result<Page<UserAdminVO>> pageAdmin(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<UserAdminVO> page = sysUserService.pageUsersByRole(current, size, "admin");
        return Result.success(page);
    }

    /**
     * 根据ID查询管理员详情
     *
     * @param id 管理员ID
     * @return 管理员信息
     */
    @GetMapping("/admin/{id}")
    public Result<UserAdminVO> getAdminById(@PathVariable Long id) {
        UserAdminVO user = sysUserService.getUserById(id);
        if (user == null || !"admin".equals(user.getRoleKey())) {
            return Result.error("管理员不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增管理员
     *
     * @param userDTO 管理员信息
     * @return 操作结果
     */
    @PostMapping("/admin")
    public Result<String> addAdmin(@Valid @RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        SysUser existUser = sysUserService.getUserByUsername(userDTO.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            SysUser emailUser = sysUserService.getUserByEmail(userDTO.getEmail());
            if (emailUser != null) {
                return Result.error("邮箱已被使用");
            }
        }
        
        // 检查密码是否提供
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        
        boolean success = sysUserService.addUser(userDTO, "admin");
        return success ? Result.success("新增管理员成功") : Result.error("新增管理员失败");
    }

    /**
     * 更新管理员
     *
     * @param userDTO 管理员信息
     * @return 操作结果
     */
    @PutMapping("/admin")
    public Result<String> updateAdmin(@Valid @RequestBody UserDTO userDTO) {
        if (userDTO.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        // 检查用户是否存在且为管理员
        UserAdminVO existUser = sysUserService.getUserById(userDTO.getId());
        if (existUser == null || !"admin".equals(existUser.getRoleKey())) {
            return Result.error("管理员不存在");
        }
        
        // 检查邮箱是否与其他用户重复
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            SysUser emailUser = sysUserService.getUserByEmail(userDTO.getEmail());
            if (emailUser != null && !emailUser.getId().equals(userDTO.getId())) {
                return Result.error("邮箱已被使用");
            }
        }
        
        boolean success = sysUserService.updateUser(userDTO);
        return success ? Result.success("更新管理员成功") : Result.error("更新管理员失败");
    }

    /**
     * 删除管理员（逻辑删除）
     *
     * @param id 管理员ID
     * @return 操作结果
     */
    @DeleteMapping("/admin/{id}")
    public Result<String> deleteAdmin(@PathVariable Long id) {
        // 检查用户是否存在且为管理员
        UserAdminVO existUser = sysUserService.getUserById(id);
        if (existUser == null || !"admin".equals(existUser.getRoleKey())) {
            return Result.error("管理员不存在");
        }
        
        boolean success = sysUserService.deleteUser(id);
        return success ? Result.success("删除管理员成功") : Result.error("删除管理员失败");
    }

    // ==================== 普通用户管理 ====================

    /**
     * 分页查询普通用户列表
     *
     * @param current 当前页，默认1
     * @param size    每页大小，默认10
     * @return 普通用户分页数据
     */
    @GetMapping("/client/page")
    public Result<Page<UserAdminVO>> pageClient(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Page<UserAdminVO> page = sysUserService.pageUsersByRole(current, size, "user");
        return Result.success(page);
    }

    /**
     * 根据ID查询普通用户详情
     *
     * @param id 普通用户ID
     * @return 普通用户信息
     */
    @GetMapping("/client/{id}")
    public Result<UserAdminVO> getClientById(@PathVariable Long id) {
        UserAdminVO user = sysUserService.getUserById(id);
        if (user == null || !"user".equals(user.getRoleKey())) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增普通用户
     *
     * @param userDTO 用户信息
     * @return 操作结果
     */
    @PostMapping("/client")
    public Result<String> addClient(@Valid @RequestBody UserDTO userDTO) {
        // 检查用户名是否已存在
        SysUser existUser = sysUserService.getUserByUsername(userDTO.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            SysUser emailUser = sysUserService.getUserByEmail(userDTO.getEmail());
            if (emailUser != null) {
                return Result.error("邮箱已被使用");
            }
        }
        
        // 检查密码是否提供
        if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
            return Result.error("密码不能为空");
        }
        
        boolean success = sysUserService.addUser(userDTO, "user");
        return success ? Result.success("新增用户成功") : Result.error("新增用户失败");
    }

    /**
     * 更新普通用户
     *
     * @param userDTO 用户信息
     * @return 操作结果
     */
    @PutMapping("/client")
    public Result<String> updateClient(@Valid @RequestBody UserDTO userDTO) {
        if (userDTO.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        // 检查用户是否存在且为普通用户
        UserAdminVO existUser = sysUserService.getUserById(userDTO.getId());
        if (existUser == null || !"user".equals(existUser.getRoleKey())) {
            return Result.error("用户不存在");
        }
        
        // 检查邮箱是否与其他用户重复
        if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty()) {
            SysUser emailUser = sysUserService.getUserByEmail(userDTO.getEmail());
            if (emailUser != null && !emailUser.getId().equals(userDTO.getId())) {
                return Result.error("邮箱已被使用");
            }
        }
        
        boolean success = sysUserService.updateUser(userDTO);
        return success ? Result.success("更新用户成功") : Result.error("更新用户失败");
    }

    /**
     * 删除普通用户（逻辑删除）
     *
     * @param id 普通用户ID
     * @return 操作结果
     */
    @DeleteMapping("/client/{id}")
    public Result<String> deleteClient(@PathVariable Long id) {
        // 检查用户是否存在且为普通用户
        UserAdminVO existUser = sysUserService.getUserById(id);
        if (existUser == null || !"user".equals(existUser.getRoleKey())) {
            return Result.error("用户不存在");
        }
        
        boolean success = sysUserService.deleteUser(id);
        return success ? Result.success("删除用户成功") : Result.error("删除用户失败");
    }
}
