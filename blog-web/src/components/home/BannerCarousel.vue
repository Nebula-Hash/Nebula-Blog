<template>
    <div class="banner-carousel" @mouseenter="pauseAutoPlay" @mouseleave="resumeAutoPlay">
        <!-- 轮播图容器 -->
        <div class="carousel-container" ref="carouselRef">
            <!-- 轮播图片列表 -->
            <transition-group :name="slideDirection" tag="div" class="carousel-slides">
                <div v-for="(banner, index) in banners" v-show="index === currentIndex" :key="banner.id"
                    class="carousel-slide" @click="handleBannerClick(banner)">
                    <!-- 图片 -->
                    <img :src="banner.imageUrl" :alt="banner.title" class="banner-image" />

                    <!-- 渐变遮罩层 -->
                    <div class="banner-overlay"></div>

                    <!-- 标题信息 -->
                    <div class="banner-info">
                        <h2 class="banner-title">{{ banner.title }}</h2>
                        <p v-if="banner.description" class="banner-description">{{ banner.description }}</p>
                    </div>
                </div>
            </transition-group>

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

        <!-- 加载状态 -->
        <div v-if="loading" class="carousel-loading">
            <n-spin size="large" />
        </div>

        <!-- 空状态 -->
        <div v-if="!loading && banners.length === 0" class="carousel-empty">
            <n-empty description="暂无轮播图" />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { NIcon, NSpin, NEmpty } from 'naive-ui'
import { ChevronBackOutline, ChevronForwardOutline } from '@vicons/ionicons5'
import { getBannerList } from '@/api/banner'
import { createErrorHandler } from '@/utils/errorHandler'

const router = useRouter()
const errorHandler = createErrorHandler('BannerCarousel')

// 状态管理
const banners = ref([])
const currentIndex = ref(0)
const loading = ref(true)
const isAutoPlaying = ref(true)
const autoPlayTimer = ref(null)
const carouselRef = ref(null)
const slideDirection = ref('slide-left') // 控制动画方向

// 配置项
const AUTO_PLAY_INTERVAL = 5000 // 自动播放间隔（毫秒）

// 获取轮播图数据
const fetchBanners = async () => {
    try {
        loading.value = true
        const response = await getBannerList()
        if (response.code === 200 && response.data) {
            banners.value = response.data
            if (banners.value.length > 0) {
                startAutoPlay()
            }
        }
    } catch (error) {
        errorHandler.handleLoad(error, '轮播图', true) // 静默失败，不影响页面其他内容
    } finally {
        loading.value = false
    }
}

// 切换到下一张（动画向左，显示右边的图）
const nextSlide = () => {
    if (banners.value.length === 0) return
    slideDirection.value = 'slide-left'
    currentIndex.value = (currentIndex.value + 1) % banners.value.length
}

// 切换到上一张（动画向右，显示左边的图）
const prevSlide = () => {
    if (banners.value.length === 0) return
    slideDirection.value = 'slide-right'
    currentIndex.value = (currentIndex.value - 1 + banners.value.length) % banners.value.length
}

// 跳转到指定幻灯片
const goToSlide = (index) => {
    // 根据目标位置决定动画方向
    if (index > currentIndex.value) {
        slideDirection.value = 'slide-left'
    } else if (index < currentIndex.value) {
        slideDirection.value = 'slide-right'
    }
    currentIndex.value = index
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
    if (banner.linkUrl) {
        if (banner.linkUrl.startsWith('http')) {
            window.open(banner.linkUrl, '_blank')
        } else {
            router.push(banner.linkUrl)
        }
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
    padding: 0 20px;
    position: relative;
}

.carousel-container {
    position: relative;
    width: 100%;
    aspect-ratio: 21 / 9;
    overflow: hidden;
    border-radius: 16px;
    background: #141517;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.3);
}

/* 轮播图片 */
.carousel-slides {
    position: relative;
    width: 100%;
    height: 100%;
}

.carousel-slide {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
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
    height: 20%;
    background: linear-gradient(to top,
            rgba(9, 10, 11, 0.95) 0%,
            rgba(9, 10, 11, 0.7) 40%,
            transparent 100%);
    pointer-events: none;
}

/* 标题信息 */
.banner-info {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 40px 60px;
    z-index: 2;
}

.banner-title {
    margin: 0 0 12px 0;
    font-size: 36px;
    font-weight: 700;
    color: #ffffff;
    line-height: 1.3;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
    max-width: 80%;
}

.banner-description {
    margin: 0;
    font-size: 16px;
    color: rgba(255, 255, 255, 0.85);
    line-height: 1.6;
    max-width: 60%;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    line-clamp: 2;
    overflow: hidden;
    text-shadow: 0 1px 4px rgba(0, 0, 0, 0.5);
}

/* 导航箭头 */
.carousel-arrow {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    width: 48px;
    height: 48px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.2);
    color: #ffffff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    z-index: 10;
    opacity: 0;
}

.carousel-arrow:hover {
    background: rgba(42, 219, 92, 0.9);
    border-color: rgba(42, 219, 92, 1);
    transform: translateY(-50%) scale(1.1);
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
    bottom: 24px;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 10px;
    z-index: 10;
}

.indicator-dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.4);
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
    padding: 0;
}

.indicator-dot:hover {
    background: rgba(255, 255, 255, 0.7);
    transform: scale(1.2);
}

.indicator-dot.active {
    width: 32px;
    border-radius: 5px;
    background: #2ADB5C;
}

/* 切换动画 - 向左滑动（新图从右进入） */
.slide-left-enter-active,
.slide-left-leave-active {
    transition: all 0.5s ease-out;
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
    transition: all 0.5s ease-out;
}

.slide-right-enter-from {
    opacity: 0;
    transform: translateX(-100%);
}

.slide-right-leave-to {
    opacity: 0;
    transform: translateX(100%);
}

/* 加载和空状态 */
.carousel-loading,
.carousel-empty {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 400px;
    background: #141517;
    border-radius: 16px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
    .banner-carousel {
        max-width: 1200px;
        padding: 0 16px;
    }

    .banner-title {
        font-size: 30px;
    }

    .banner-description {
        font-size: 15px;
    }

    .banner-info {
        padding: 30px 40px;
    }
}

@media (max-width: 768px) {
    .banner-carousel {
        padding: 0 12px;
    }

    .carousel-container {
        aspect-ratio: 4 / 3;
    }

    .banner-title {
        font-size: 24px;
        max-width: 90%;
    }

    .banner-description {
        font-size: 14px;
        max-width: 90%;
        -webkit-line-clamp: 1;
        line-clamp: 1;
    }

    .banner-info {
        padding: 20px 24px;
    }

    .carousel-arrow {
        width: 40px;
        height: 40px;
        opacity: 1;
    }

    .carousel-arrow-left {
        left: 16px;
    }

    .carousel-arrow-right {
        right: 16px;
    }

    .carousel-indicators {
        bottom: 16px;
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
        padding: 0 8px;
    }

    .carousel-container {
        aspect-ratio: 1 / 1;
        border-radius: 12px;
    }

    .banner-title {
        font-size: 20px;
    }

    .banner-description {
        display: none;
    }

    .banner-info {
        padding: 16px 20px;
    }

    .carousel-arrow {
        width: 36px;
        height: 36px;
    }
}
</style>
