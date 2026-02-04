/**
 * 组件懒加载工具
 * 提供统一的组件懒加载功能
 */
import { defineAsyncComponent, h } from 'vue'
import { NSpin } from 'naive-ui'

/**
 * 创建懒加载组件
 * @param {Function} loader - 组件加载函数
 * @param {Object} options - 配置选项
 * @returns {Component} 异步组件
 */
export function createLazyComponent(loader, options = {}) {
    const {
        delay = 200,
        timeout = 10000,
        errorComponent = null,
        loadingComponent = null,
        onError = null
    } = options

    return defineAsyncComponent({
        loader,
        delay,
        timeout,
        errorComponent,
        loadingComponent: loadingComponent || {
            setup() {
                return () => h('div', {
                    style: {
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        padding: '20px'
                    }
                }, [h(NSpin, { size: 'medium' })])
            }
        },
        onError: (error, retry, fail, attempts) => {
            console.error('[LazyComponent] 组件加载失败:', error)

            if (onError) {
                onError(error, retry, fail, attempts)
            } else {
                // 默认重试逻辑：最多重试 3 次
                if (attempts <= 3) {
                    console.log(`[LazyComponent] 重试加载组件 (${attempts}/3)`)
                    retry()
                } else {
                    fail()
                }
            }
        }
    })
}

export default {
    createLazyComponent
}
