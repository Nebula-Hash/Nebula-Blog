<template>
    <div class="carousel-container" v-if="banners.length > 0" @mousedown="onDragStart" @mouseup="onDragEnd"
        @mouseleave="onDragEnd">
        <div class="carousel-track">
            <!-- 上一张 -->
            <div class="slide side left" @click="goPrev">
                <img :src="prevBanner.imageUrl" :alt="prevBanner.title" />
                <div class="mask"></div>
            </div>

            <!-- 当前 -->
            <div class="slide main" @click="goLink(currentBanner)">
                <img :src="currentBanner.imageUrl" :alt="currentBanner.title" />
                <div class="title" :key="currentBanner.id">
                    {{ currentBanner.title }}
                </div>
            </div>

            <!-- 下一张 -->
            <div class="slide side right" @click="goNext">
                <img :src="nextBanner.imageUrl" :alt="nextBanner.title" />
                <div class="mask"></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { getBannerList } from '@/api/banner'

const router = useRouter()
const banners = ref([])
const currentIndex = ref(0)

let timer = null
let startX = 0

/* ========= 数据 ========= */
const prevIndex = computed(() =>
    (currentIndex.value - 1 + banners.value.length) % banners.value.length
)
const nextIndex = computed(() =>
    (currentIndex.value + 1) % banners.value.length
)

const currentBanner = computed(() => banners.value[currentIndex.value])
const prevBanner = computed(() => banners.value[prevIndex.value])
const nextBanner = computed(() => banners.value[nextIndex.value])

/* ========= 行为 ========= */
const goPrev = () => {
    resetTimer()
    currentIndex.value = prevIndex.value
}

const goNext = () => {
    resetTimer()
    currentIndex.value = nextIndex.value
}

const goLink = (banner) => {
    if (!banner?.linkUrl) return
    resetTimer()
    if (banner.linkUrl.startsWith('http')) {
        window.open(banner.linkUrl, '_blank')
    } else {
        router.push(banner.linkUrl)
    }
}

/* ========= 拖拽 ========= */
const onDragStart = (e) => {
    startX = e.clientX
}

const onDragEnd = (e) => {
    if (!startX) return
    const diff = e.clientX - startX
    if (Math.abs(diff) > 60) {
        diff > 0 ? goPrev() : goNext()
    }
    startX = 0
}

/* ========= 自动轮播 ========= */
const startTimer = () => {
    timer = setInterval(goNext, 5000)
}

const resetTimer = () => {
    clearInterval(timer)
    startTimer()
}

/* ========= 初始化 ========= */
const loadBanners = async () => {
    const res = await getBannerList()
    banners.value = res.data || []
}

onMounted(async () => {
    await loadBanners()
    startTimer()
})

onBeforeUnmount(() => {
    clearInterval(timer)
})
</script>

<style scoped>
.carousel-container {
    width: 100%;
    min-height: 320px;
    user-select: none;
}

.carousel-track {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 24px;
    overflow: visible;
}

/* 通用 */
.slide {
    position: relative;
    border-radius: 14px;
    overflow: hidden;
    transition: transform 0.45s ease, opacity 0.45s ease;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.35);
}

.slide img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

/* 主图 */
.slide.main {
    width: 60%;
    aspect-ratio: 16 / 9;
    transform: scale(1);
    cursor: pointer;
}

/* 左右小图 */
.slide.side {
    width: 20%;
    aspect-ratio: 16 / 9;
    transform: scale(0.85);
    cursor: pointer;
}

.slide.side .mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
}

/* 标题动画 */
.title {
    position: absolute;
    left: 0;
    bottom: 0;
    width: 100%;
    padding: 26px;
    font-size: 26px;
    font-weight: bold;
    color: #fff;
    background: linear-gradient(transparent, rgba(0, 0, 0, 0.85));
    opacity: 0;
    transform: translateY(12px);
    animation: titleIn 0.55s ease forwards;
}

@keyframes titleIn {
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
