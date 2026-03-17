package com.nebula.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 事务补偿辅助工具
 * 用于注册事务回滚后的补偿动作，处理数据库事务之外的副作用
 *
 * @author Nebula-Hash
 * @date 2026/2/25
 */
@Slf4j
public final class TransactionCompensationHelper {

    private TransactionCompensationHelper() {
    }

    /**
     * 注册事务回滚补偿动作
     *
     * @param actionName 补偿动作名称
     * @param rollbackAction 回滚补偿动作
     */
    public static void registerRollbackAction(String actionName, Runnable rollbackAction) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_ROLLED_BACK) {
                    return;
                }
                try {
                    rollbackAction.run();
                    log.info("事务回滚补偿执行成功: {}", actionName);
                } catch (Exception e) {
                    log.warn("事务回滚补偿执行失败: {}", actionName, e);
                }
            }
        });
    }
}
