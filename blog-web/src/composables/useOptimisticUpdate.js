import { ref } from 'vue'

/**
 * 乐观更新组合式函数
 * 实现乐观更新模式：先更新UI，再发送请求，失败时回滚
 * @param {Function} updateFn - 更新函数（发送请求）
 * @param {Function} rollbackFn - 回滚函数（恢复状态）
 * @returns {Object} { execute, isPending, error }
 */
export function useOptimisticUpdate(updateFn, rollbackFn) {
    const isPending = ref(false)
    const error = ref(null)

    /**
     * 执行乐观更新
     * @param {*} optimisticData - 乐观更新的数据（用于立即更新UI）
     * @param {*} rollbackData - 回滚数据（用于失败时恢复）
     * @returns {Promise<*>} 请求结果
     */
    const execute = async (optimisticData, rollbackData) => {
        isPending.value = true
        error.value = null

        try {
            // 执行更新函数（发送请求）
            const result = await updateFn(optimisticData)
            return result
        } catch (err) {
            // 请求失败，执行回滚
            error.value = err
            if (rollbackFn && rollbackData !== undefined) {
                rollbackFn(rollbackData)
            }
            throw err
        } finally {
            isPending.value = false
        }
    }

    return {
        execute,
        isPending,
        error
    }
}
