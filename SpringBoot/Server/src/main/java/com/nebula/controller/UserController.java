package com.nebula.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nebula.result.Result;
import com.nebula.entity.SysUser;
import com.nebula.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 *
 * @author Nebula-Hash
 * @date 2025/11/25
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    /**
     * 分页查询用户列表
     *
     * @param current  当前页，默认1
     * @param size     每页大小，默认10
     * @param username 用户名（可选，模糊查询）
     * @return 用户分页数据
     */
    @GetMapping("/page")
    public Result<Page<SysUser>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String username) {
        Page<SysUser> page = sysUserService.pageUsers(current, size, username);
        return Result.success(page);
    }

    /**
     * 查询所有用户
     *
     * @return 用户列表
     */
    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        List<SysUser> list = sysUserService.list();
        return Result.success(list);
    }

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    public Result<SysUser> getByUsername(@PathVariable String username) {
        SysUser user = sysUserService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 新增用户
     *
     * @param user 用户信息
     * @return 操作结果
     */
    @PostMapping
    public Result<String> save(@RequestBody SysUser user) {
        // 检查用户名是否已存在
        SysUser existUser = sysUserService.getUserByUsername(user.getUsername());
        if (existUser != null) {
            return Result.error("用户名已存在");
        }
        
        boolean success = sysUserService.save(user);
        return success ? Result.success("新增成功") : Result.error("新增失败");
    }

    /**
     * 更新用户
     *
     * @param user 用户信息
     * @return 操作结果
     */
    @PutMapping
    public Result<String> update(@RequestBody SysUser user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }
        
        // 如果修改了用户名，检查新用户名是否已存在
        if (user.getUsername() != null) {
            SysUser existUser = sysUserService.getUserByUsername(user.getUsername());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                return Result.error("用户名已存在");
            }
        }
        
        boolean success = sysUserService.updateById(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = sysUserService.removeById(id);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 批量删除用户（逻辑删除）
     *
     * @param ids 用户ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        boolean success = sysUserService.removeByIds(ids);
        return success ? Result.success("批量删除成功") : Result.error("批量删除失败");
    }
}
