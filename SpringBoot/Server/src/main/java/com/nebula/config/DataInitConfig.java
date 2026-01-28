package com.nebula.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nebula.entity.SysRole;
import com.nebula.entity.SysUser;
import com.nebula.mapper.SysRoleMapper;
import com.nebula.mapper.SysUserMapper;
import com.nebula.service.helper.AuthHelper;
import com.nebula.utils.PasswordUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * 系统数据初始化配置
 * <p>
 * 应用启动时自动检查并初始化角色和管理员账号
 *
 * @author Nebula-Hash
 * @date 2026/1/28
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitConfig implements CommandLineRunner {

    private final SysRoleMapper sysRoleMapper;
    private final SysUserMapper sysUserMapper;

    /**
     * 默认管理员用户名（可通过配置文件覆盖）
     */
    @Value("${nebula.admin.username:admin}")
    private String adminUsername;

    /**
     * 默认管理员密码（可通过配置文件覆盖）
     */
    @Value("${nebula.admin.password:123456}")
    private String adminPassword;

    /**
     * 默认管理员昵称
     */
    @Value("${nebula.admin.nickname:系统管理员}")
    private String adminNickname;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void run(@NotNull String... args) {
        log.info("========== 开始系统数据初始化检查 ==========");

        // 1. 初始化角色
        initRoles();

        // 2. 初始化管理员账号
        initAdminUser();

        log.info("========== 系统数据初始化检查完成 ==========");
    }

    /**
     * 初始化系统角色
     */
    private void initRoles() {
        // 检查并创建管理员角色
        if (!roleExists(AuthHelper.ADMIN_ROLE_KEY)) {
            SysRole adminRole = new SysRole();
            adminRole.setRoleName("管理员");
            adminRole.setRoleKey(AuthHelper.ADMIN_ROLE_KEY);
            adminRole.setRoleSort(1);
            adminRole.setStatus(1);
            sysRoleMapper.insert(adminRole);
            log.info("已创建管理员角色: {}", AuthHelper.ADMIN_ROLE_KEY);
        }

        // 检查并创建普通用户角色
        if (!roleExists(AuthHelper.USER_ROLE_KEY)) {
            SysRole userRole = new SysRole();
            userRole.setRoleName("普通用户");
            userRole.setRoleKey(AuthHelper.USER_ROLE_KEY);
            userRole.setRoleSort(2);
            userRole.setStatus(1);
            sysRoleMapper.insert(userRole);
            log.info("已创建普通用户角色: {}", AuthHelper.USER_ROLE_KEY);
        }
    }

    /**
     * 初始化管理员账号
     */
    private void initAdminUser() {
        // 检查是否已存在管理员账号
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.eq(SysRole::getRoleKey, AuthHelper.ADMIN_ROLE_KEY);
        SysRole adminRole = sysRoleMapper.selectOne(roleQuery);

        if (adminRole == null) {
            log.warn("管理员角色不存在，跳过管理员账号初始化");
            return;
        }

        // 检查是否已有管理员用户
        LambdaQueryWrapper<SysUser> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(SysUser::getRoleId, adminRole.getId());
        Long adminCount = sysUserMapper.selectCount(userQuery);

        if (adminCount > 0) {
            log.info("系统已存在 {} 个管理员账号，跳过初始化", adminCount);
            return;
        }

        // 创建默认管理员账号
        SysUser adminUser = new SysUser();
        adminUser.setUsername(adminUsername);
        adminUser.setPassword(PasswordUtils.encode(adminPassword));
        adminUser.setNickname(adminNickname);
        adminUser.setRoleId(adminRole.getId());
        adminUser.setStatus(1);
        sysUserMapper.insert(adminUser);

        log.info("========================================");
        log.info("已创建默认管理员账号");
        log.info("用户名: {}", adminUsername);
        log.info("密码: {}", adminPassword);
        log.info("请登录后立即修改密码！");
        log.info("========================================");
    }

    /**
     * 检查角色是否存在
     */
    private boolean roleExists(String roleKey) {
        LambdaQueryWrapper<SysRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysRole::getRoleKey, roleKey);
        return sysRoleMapper.selectCount(queryWrapper) > 0;
    }
}
