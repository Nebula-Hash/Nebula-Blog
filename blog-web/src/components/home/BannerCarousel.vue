<template>
    <div v-if="!loading && banners.length > 0" class="banner-carousel" @mouseenter="pauseAutoPlay"
        @mouseleave="resumeAutoPlay">
        <!-- 轮播图容器 -->
        <div class="carousel-container" ref="carouselRef">
            <!-- 轮播图片列表 -->
            <div class="carousel-slides">
                <div ref="trackRef" class="carousel-track" :class="{ 'no-transition': !enableTransition }"
                    :style="trackStyle" @transitionend="handleTrackTransitionEnd">
                    <div v-for="(banner, index) in displayBanners" :key="`${banner.id}-${index}`" class="carousel-slide"
                        @click="handleBannerClick(banner)">
                        <!-- 图片 - 使用picture标签支持WebP -->
                        <picture>
                            <source v-if="banner.imageUrl.endsWith('.webp')" :srcset="banner.imageUrl" type="image/webp" />
                            <source v-else-if="supportsWebP && /\/\d{4}-\d{2}-\d{2}/.test(banner.imageUrl)"
                                :srcset="banner.imageUrl" type="image/webp" />
                            <img :src="banner.imageUrl" :alt="banner.title" class="banner-image" />
                        </picture>

                        <!-- 渐变遮罩层 -->
                        <div class="banner-overlay"></div>

                        <!-- 标题信息 -->
                        <div class="banner-info">
                            <h2 class="banner-title">{{ banner.title }}</h2>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 左右导航箭头 -->
            <button class="carousel-arrow carousel-arrow-left" @click="prevSlide" aria-label="上一张">
                <n-icon :size="24" :component="ChevronBackOutline" />
            </button>
            <button class="carousel-arrow carousel-arrow-right" @click="nextSlide" aria-label="下一张">
                <n-icon :size="24" :component="ChevronForwardOutline" />
            </button>

            <!-- 指示器 -->
            <div class="carousel-indicators">
                <button v-for="(banner, index) in banners" :key="banner.id"
                    :class="['indicator-dot', { active: index === currentIndex }]" @click="goToSlide(index)"
                    :aria-label="`跳转到第 ${index + 1} 张`"></button>
            </div>
        </div>


    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon, NSpin, NEmpty } from 'naive-ui'
import { ChevronBackOutline, ChevronForwardOutline } from '@vicons/ionicons5'
import { useCacheStore } from '@/stores'

const router = useRouter()
const cacheStore = useCacheStore()

// WebP支持检测
const supportsWebP = ref(false)

// 检测浏览器是否支持WebP
const checkWebPSupport = () => {
    const canvas = document.createElement('canvas')
    if (canvas.getContext && canvas.getContext('2d')) {
        return canvas.toDataURL('image/webp').indexOf('data:image/webp') === 0
    }
    return false
}

supportsWebP.value = checkWebPSupport()

// 状态管理
const banners = ref([])
const currentIndex = ref(0) // 指示器索引（真实列表）
const trackIndex = ref(1) // 轨道索引（包含首尾克隆）
const loading = ref(true)
const isAutoPlaying = ref(true)
const autoPlayTimer = ref(null)
const carouselRef = ref(null)
const trackRef = ref(null)
const enableTransition = ref(true)
const isTransitioning = ref(false)

const displayBanners = computed(() => {
    if (!banners.value || banners.value.length === 0) return []
    const first = banners.value[0]
    const last = banners.value[banners.value.length - 1]
    return [last, ...banners.value, first]
})

const trackStyle = computed(() => {
    return {
        transform: `translateX(-${trackIndex.value * 100}%)`
    }
})

// 配置项
const AUTO_PLAY_INTERVAL = 5000 // 自动播放间隔（毫秒）

// 获取轮播图数据
const fetchBanners = async () => {
    try {
        loading.value = true
        banners.value = await cacheStore.fetchBannerList()
        if (banners.value.length > 0) {
            currentIndex.value = 0
            trackIndex.value = 1
            startAutoPlay()
        }
    } catch (error) {
        console.error('[BannerCarousel] 加载轮播图失败:', error)
    } finally {
        loading.value = false
    }
}

// 切换到下一张（向左滑动）
const nextSlide = () => {
    if (banners.value.length === 0) return
    if (isTransitioning.value) return
    isTransitioning.value = true
    enableTransition.value = true
    trackIndex.value = trackIndex.value + 1
    currentIndex.value = (currentIndex.value + 1) % banners.value.length
}

// 切换到上一张（向右滑动）
const prevSlide = () => {
    if (banners.value.length === 0) return
    if (isTransitioning.value) return
    isTransitioning.value = true
    enableTransition.value = true
    trackIndex.value = trackIndex.value - 1
    currentIndex.value = (currentIndex.value - 1 + banners.value.length) % banners.value.length
}

// 跳转到指定幻灯片
const goToSlide = (index) => {
    if (banners.value.length === 0) return
    if (isTransitioning.value) return
    isTransitioning.value = true
    enableTransition.value = true
    currentIndex.value = index
    trackIndex.value = index + 1
}

const handleTrackTransitionEnd = (event) => {
    if (event.target !== trackRef.value) return
    if (event.propertyName && event.propertyName !== 'transform') return
    const len = banners.value.length
    if (len === 0) return

    const el = trackRef.value

    // 到达克隆页时：先在下一帧关闭过渡，再在随后一帧跳回真实位置，避免出现“走马灯回滚”动画
    if (trackIndex.value === 0) {
        enableTransition.value = false
        if (el) {
            el.style.transition = 'none'
            void el.offsetHeight
        }
        trackIndex.value = len
        nextTick(() => {
            if (el) {
                el.style.transform = `translateX(-${trackIndex.value * 100}%)`
                void el.offsetHeight
            }
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    if (el) {
                        el.style.transition = ''
                    }
                    enableTransition.value = true
                    isTransitioning.value = false
                })
            })
        })
        return
    }

    if (trackIndex.value === len + 1) {
        enableTransition.value = false
        if (el) {
            el.style.transition = 'none'
            void el.offsetHeight
        }
        trackIndex.value = 1
        nextTick(() => {
            if (el) {
                el.style.transform = `translateX(-${trackIndex.value * 100}%)`
                void el.offsetHeight
            }
            requestAnimationFrame(() => {
                requestAnimationFrame(() => {
                    if (el) {
                        el.style.transition = ''
                    }
                    enableTransition.value = true
                    isTransitioning.value = false
                })
            })
        })
        return
    }

    isTransitioning.value = false
}

// 开始自动播放
const startAutoPlay = () => {
    if (autoPlayTimer.value) {
        clearInterval(autoPlayTimer.value)
    }
    autoPlayTimer.value = setInterval(() => {
        if (isAutoPlaying.value) {
            nextSlide()
        }
    }, AUTO_PLAY_INTERVAL)
}

// 暂停自动播放
const pauseAutoPlay = () => {
    isAutoPlaying.value = false
}

// 恢复自动播放
const resumeAutoPlay = () => {
    isAutoPlaying.value = true
}

// 处理轮播图点击
const handleBannerClick = (banner) => {
    if (banner.articleId) {
        // 跳转到文章详情页
        router.push(`/article/${banner.articleId}`)
    }
}



// 键盘导航支持
const handleKeydown = (event) => {
    if (event.key === 'ArrowLeft') {
        prevSlide()
    } else if (event.key === 'ArrowRight') {
        nextSlide()
    }
}

// 触摸滑动支持
let touchStartX = 0
let touchEndX = 0

const handleTouchStart = (event) => {
    touchStartX = event.changedTouches[0].screenX
}

const handleTouchEnd = (event) => {
    touchEndX = event.changedTouches[0].screenX
    handleSwipe()
}

const handleSwipe = () => {
    const swipeThreshold = 50
    if (touchStartX - touchEndX > swipeThreshold) {
        nextSlide()
    } else if (touchEndX - touchStartX > swipeThreshold) {
        prevSlide()
    }
}

// 生命周期
onMounted(() => {
    fetchBanners()
    window.addEventListener('keydown', handleKeydown)

    if (carouselRef.value) {
        // 添加 passive 选项以提高滚动性能
        carouselRef.value.addEventListener('touchstart', handleTouchStart, { passive: true })
        carouselRef.value.addEventListener('touchend', handleTouchEnd, { passive: true })
    }
})

onUnmounted(() => {
    if (autoPlayTimer.value) {
        clearInterval(autoPlayTimer.value)
    }
    window.removeEventListener('keydown', handleKeydown)

    if (carouselRef.value) {
        carouselRef.value.removeEventListener('touchstart', handleTouchStart)
        carouselRef.value.removeEventListener('touchend', handleTouchEnd)
    }
})
</script>

<style scoped>
.banner-carousel {
    width: 100%;
    max-width: 1000px;
    margin: 0 auto;
    padding: 0 var(--spacing-lg);
    position: relative;
}

.carousel-container {
    position: relative;
    width: 100%;
    aspect-ratio: 21 / 9;
    overflow: hidden;
    border-radius: var(--radius-2xl);
    background: var(--surface-primary);
    box-shadow: var(--shadow-2xl);
    border: 1px solid var(--border-secondary);
    transition: all var(--transition-base);
}

.carousel-container:hover {
    box-shadow: 0 16px 48px var(--shadow-color);
}

/* 轮播图片 */
.carousel-slides {
    position: relative;
    width: 100%;
    height: 100%;
}

.carousel-track {
    display: flex;
    width: 100%;
    height: 100%;
    will-change: transform;
    transition: transform 600ms ease;
}

.carousel-track.no-transition {
    transition: none;
}

.carousel-slide {
    position: relative;
    width: 100%;
    height: 100%;
    flex: 0 0 100%;
    cursor: pointer;
}

.banner-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

/* 渐变遮罩层 */
.banner-overlay {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 30%;
    background: linear-gradient(to top,
            rgba(0, 0, 0, 0.9) 0%,
            rgba(0, 0, 0, 0.6) 50%,
            transparent 100%);
    pointer-events: none;
}

/* 标题信息 */
.banner-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: var(--spacing-2xl) var(--spacing-3xl);
    z-index: 2;
}

.banner-title {
    margin: 0;
    font-size: var(--font-size-4xl);
    font-weight: var(--font-weight-bold);
    color: white;
    line-height: var(--line-height-tight);
    text-shadow: 0 4px 12px rgba(0, 0, 0, 0.6);
    max-width: 80%;
    transition: transform var(--transition-base);
}

.carousel-slide:hover .banner-title {
    transform: translateY(-4px);
}

/* 导航箭头 */
.carousel-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 48px;
    height: 48px;
    border-radius: var(--radius-full);
    background: rgba(255, 255, 255, 0.12);
    backdrop-filter: var(--backdrop-blur);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: white;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all var(--transition-fast);
    z-index: 10;
    opacity: 0;
}

.carousel-arrow:hover {
    background: var(--color-primary);
    border-color: var(--color-primary);
    transform: translateY(-50%) scale(1.1);
    box-shadow: 0 8px 24px var(--color-primary-alpha-30);
}

.carousel-arrow-left {
    left: 30px;
}

.carousel-arrow-right {
    right: 30px;
}

.banner-carousel:hover .carousel-arrow {
    opacity: 1;
}

/* 指示器 */
.carousel-indicators {
    position: absolute;
    bottom: var(--spacing-lg);
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: var(--spacing-sm);
    z-index: 10;
}

.indicator-dot {
    width: 10px;
    height: 10px;
    border-radius: var(--radius-full);
    background: rgba(255, 255, 255, 0.4);
    border: none;
    cursor: pointer;
    transition: all var(--transition-fast);
    padding: 0;
}

.indicator-dot:hover {
    background: rgba(255, 255, 255, 0.7);
    transform: scale(1.2);
}

.indicator-dot.active {
    width: 32px;
    border-radius: var(--radius-sm);
    background: var(--color-primary);
    box-shadow: 0 0 12px var(--color-primary-alpha-30);
}

/* 切换动画 - 向左滑动（新图从右进入） */
.slide-left-enter-active,
.slide-left-leave-active {
    transition: all var(--transition-slower) var(--ease-out);
}

.slide-left-enter-from {
    opacity: 0;
    transform: translateX(100%);
}

.slide-left-leave-to {
    opacity: 0;
    transform: translateX(-100%);
}

/* 切换动画 - 向右滑动（新图从左进入） */
.slide-right-enter-active,
.slide-right-leave-active {
    transition: all var(--transition-slower) var(--ease-out);
}

.slide-right-enter-from {
    opacity: 0;
    transform: translateX(-100%);
}

.slide-right-leave-to {
    opacity: 0;
    transform: translateX(100%);
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .banner-carousel {
        max-width: 1200px;
        padding: 0 var(--spacing-md);
    }

    .banner-title {
        font-size: var(--font-size-3xl);
    }

    .banner-info {
        padding: var(--spacing-xl) var(--spacing-2xl);
    }
}

@media (max-width: 768px) {
    .banner-carousel {
        padding: 0 var(--spacing-sm);
    }

    .carousel-container {
        aspect-ratio: 4 / 3;
        border-radius: var(--radius-xl);
    }

    .banner-title {
        font-size: var(--font-size-2xl);
        max-width: 90%;
    }

    .banner-info {
        padding: var(--spacing-lg) var(--spacing-xl);
    }

    .carousel-arrow {
        width: 40px;
        height: 40px;
        opacity: 1;
    }

    .carousel-arrow-left {
        left: var(--spacing-md);
    }

    .carousel-arrow-right {
        right: var(--spacing-md);
    }

    .carousel-indicators {
        bottom: var(--spacing-md);
    }

    .indicator-dot {
        width: 8px;
        height: 8px;
    }

    .indicator-dot.active {
        width: 24px;
    }
}

@media (max-width: 480px) {
    .banner-carousel {
        padding: 0 var(--spacing-xs);
    }

    .carousel-container {
        aspect-ratio: 1 / 1;
        border-radius: var(--radius-lg);
    }

    .banner-title {
        font-size: var(--font-size-xl);
    }

    .banner-info {
        padding: var(--spacing-md) var(--spacing-lg);
    }

    .carousel-arrow {
        width: 36px;
        height: 36px;
    }
}
</style>
