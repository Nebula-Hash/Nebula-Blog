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

/**
 * 创建带预加载的懒加载组件
 * @param {Function} loader - 组件加载函数
 * @param {Object} options - 配置选项
 * @returns {Component} 异步组件
 */
export function createPreloadComponent(loader, options = {}) {
    const component = createLazyComponent(loader, options)

    // 预加载组件
    if (options.preload) {
        loader().catch(error => {
            console.warn('[LazyComponent] 预加载失败:', error)
        })
    }

    return component
}

/**
 * 批量创建懒加载组件
 * @param {Object} components - 组件映射 { name: loader }
 * @param {Object} options - 配置选项
 * @returns {Object} 懒加载组件映射
 */
export function createLazyComponents(components, options = {}) {
    const lazyComponents = {}

    Object.keys(components).forEach(name => {
        lazyComponents[name] = createLazyComponent(components[name], options)
    })

    return lazyComponents
}

/**
 * 路由级别的懒加载组件
 * 专门用于路由组件的懒加载
 * @param {Function} loader - 组件加载函数
 * @returns {Component} 异步组件
 */
export function createRouteComponent(loader) {
    return createLazyComponent(loader, {
        delay: 0, // 路由组件不需要延迟
        timeout: 15000, // 路由组件超时时间更长
        onError: (error, retry, fail, attempts) => {
            console.error('[RouteComponent] 路由组件加载失败:', error)

            if (attempts <= 2) {
                console.log(`[RouteComponent] 重试加载路由组件 (${attempts}/2)`)
                setTimeout(() => retry(), 1000)
            } else {
                fail()
            }
        }
    })
}

export default {
    createLazyComponent,
    createPreloadComponent,
    createLazyComponents,
    createRouteComponent
}
