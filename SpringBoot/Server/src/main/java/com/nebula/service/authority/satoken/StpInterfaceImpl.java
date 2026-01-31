package com.nebula.service.authority.satoken;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现类
 * 用于获取当前账号的权限列表和角色列表
 *
 * @author Nebula-Hash
 * @date 2026/1/27
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 返回一个账号所拥有的权限码集合
     *
     * @param loginId   账号ID
     * @param loginType 账号体系标识
     * @return 权限码集合
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 暂不实现细粒度权限，返回空列表
        return new ArrayList<>();
    }

    /**
     * 返回一个账号所拥有的角色标识集合
     *
     * @param loginId   账号ID
     * @param loginType 账号体系标识
     * @return 角色标识集合
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();

        // 从 Session 中获取角色标识
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session != null) {
            String roleKey = session.getString("roleKey");
            if (roleKey != null && !roleKey.isEmpty()) {
                roleList.add(roleKey);
            }
        }

        return roleList;
    }
}
