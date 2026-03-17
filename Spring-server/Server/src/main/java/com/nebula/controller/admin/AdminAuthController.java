package com.nebula.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import com.nebula.controller.config.AdminController;
import com.nebula.dto.LoginDTO;
import com.nebula.result.Result;
import com.nebula.service.authority.AuthService;
import com.nebula.vo.LoginVO;
import com.nebula.vo.UserInfoVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @author Nebula-Hash
 * @date 2026/1/22
 */
@AdminController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AuthService authService;

    /**
     * 用户登录（需校验管理员角色）
     *
     * @param loginDTO 登录参数
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.adminLogin(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户登出
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        authService.logout();
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoVO> getCurrentUser() {
        UserInfoVO userInfo = authService.getCurrentUser();
        return Result.success(userInfo);
    }

    /**
     * 刷新 Token（需校验管理员角色）
     *
     * @return 新的 Token 信息
     */
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken() {
        LoginVO loginVO = authService.adminRefreshToken();
        return Result.success("Token 刷新成功", loginVO);
    }

    /**
     * 检查登录状态
     *
     * @return 是否已登录
     */
    @GetMapping("/check")
    public Result<Boolean> checkLogin() {
        boolean isLogin = StpUtil.isLogin();
        return Result.success(isLogin);
    }
}
