<template>
    <div class="error-page">
        <div ref="particles" class="particles"></div>

        <div class="error-content">
            <div class="glitch" data-text="404">404</div>
            <h2 class="subtitle">页面迷失在星际中</h2>
            <div class="description">
                <p>我们无法找到您请求的页面</p>
                <p>它可能已被黑洞吞噬或穿越到了平行宇宙</p>
            </div>

            <div class="tech-circle">
                <div class="circle"></div>
                <div class="circle"></div>
                <div class="circle"></div>
            </div>

            <router-link to="/" class="home-button">
                <span class="button-text">返回主星系</span>
                <span class="button-icon">→</span>
            </router-link>
        </div>
    </div>
</template>

<script>
import { onMounted, ref } from 'vue';

export default {
    setup() {
        const particles = ref(null);

        onMounted(() => {
            createParticles();
        });

        const createParticles = () => {
            const particleCount = 150;
            const container = particles.value;

            for (let i = 0; i < particleCount; i++) {
                const particle = document.createElement('div');
                particle.className = 'particle';

                // 随机位置和大小
                const size = Math.random() * 3 + 1;
                const posX = Math.random() * 100;
                const posY = Math.random() * 100;
                const delay = Math.random() * 10;
                const duration = 20 + Math.random() * 30;

                // 随机透明度
                const opacity = Math.random() * 0.7 + 0.3;

                // 设置样式
                particle.style.cssText = `
          width: ${size}px;
          height: ${size}px;
          left: ${posX}%;
          top: ${posY}%;
          background: white;
          position: absolute;
          border-radius: 50%;
          opacity: ${opacity};
          animation: float ${duration}s linear ${delay}s infinite;
          box-shadow: 0 0 ${size * 2}px ${size}px rgba(255, 255, 255, 0.2);
        `;

                container.appendChild(particle);
            }
        };

        return {
            particles
        };
    }
};
</script>

<style scoped>
/* 基础样式 */
.error-page {
    position: relative;
    width: 100%;
    height: 100vh;
    overflow: hidden;
    background: linear-gradient(to bottom, #0f0c29, #302b63, #24243e);
    color: white;
    font-family: 'Arial', sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
}

/* 粒子背景 */
.particles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    overflow: hidden;
    z-index: 1;
}

/* 内容区域 */
.error-content {
    position: relative;
    z-index: 10;
    max-width: 800px;
    padding: 2rem;
}

/* 404文字效果 */
.glitch {
    font-size: 10rem;
    font-weight: 700;
    text-transform: uppercase;
    position: relative;
    color: #fff;
    margin: 0;
    line-height: 1;
    animation: glitch-effect 5s infinite;
}

.glitch:before,
.glitch:after {
    content: attr(data-text);
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

.glitch:before {
    left: 2px;
    text-shadow: -2px 0 #ff00c1;
    clip: rect(44px, 450px, 56px, 0);
    animation: glitch-anim 5s infinite linear alternate-reverse;
}

.glitch:after {
    left: -2px;
    text-shadow: -2px 0 #00fff9, 2px 2px #ff00c1;
    clip: rect(44px, 450px, 56px, 0);
    animation: glitch-anim2 5s infinite linear alternate-reverse;
}

.subtitle {
    font-size: 1.5rem;
    margin: 1rem 0;
    color: #a0a0ff;
    text-shadow: 0 0 10px rgba(160, 160, 255, 0.5);
}

.description {
    font-size: 1.2rem;
    margin-bottom: 2rem;
    color: #ccc;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
}

/* 科技圆环效果 */
.tech-circle {
    position: relative;
    width: 200px;
    height: 200px;
    margin: 2rem auto;
}

.tech-circle .circle {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    border: 2px solid rgba(0, 255, 255, 0.5);
    border-radius: 50%;
    animation: pulse 4s infinite ease-in-out;
}

.tech-circle .circle:nth-child(1) {
    width: 180px;
    height: 180px;
    animation-delay: 0s;
}

.tech-circle .circle:nth-child(2) {
    width: 120px;
    height: 120px;
    animation-delay: 1s;
}

.tech-circle .circle:nth-child(3) {
    width: 60px;
    height: 60px;
    animation-delay: 2s;
}

/* 返回按钮 */
.home-button {
    display: inline-flex;
    align-items: center;
    padding: 0.8rem 1.5rem;
    background: linear-gradient(45deg, #6e00ff, #00d4ff);
    color: white;
    text-decoration: none;
    border-radius: 50px;
    font-size: 1.1rem;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
    box-shadow: 0 0 15px rgba(0, 212, 255, 0.5);
    z-index: 10;
}

.home-button:hover {
    transform: translateY(-3px);
    box-shadow: 0 0 25px rgba(0, 212, 255, 0.8);
}

.home-button .button-icon {
    margin-left: 0.5rem;
    transition: all 0.3s ease;
}

.home-button:hover .button-icon {
    transform: translateX(5px);
}

/* 动画效果 */
@keyframes glitch-effect {

    0%,
    100% {
        transform: translate(0);
    }

    20% {
        transform: translate(-3px, 3px);
    }

    40% {
        transform: translate(-3px, -3px);
    }

    60% {
        transform: translate(3px, 3px);
    }

    80% {
        transform: translate(3px, -3px);
    }
}

@keyframes glitch-anim {
    0% {
        clip: rect(31px, 9999px, 94px, 0);
    }

    10% {
        clip: rect(112px, 9999px, 76px, 0);
    }

    20% {
        clip: rect(85px, 9999px, 77px, 0);
    }

    30% {
        clip: rect(27px, 9999px, 97px, 0);
    }

    40% {
        clip: rect(64px, 9999px, 98px, 0);
    }

    50% {
        clip: rect(61px, 9999px, 85px, 0);
    }

    60% {
        clip: rect(99px, 9999px, 114px, 0);
    }

    70% {
        clip: rect(34px, 9999px, 115px, 0);
    }

    80% {
        clip: rect(98px, 9999px, 129px, 0);
    }

    90% {
        clip: rect(43px, 9999px, 96px, 0);
    }

    100% {
        clip: rect(82px, 9999px, 64px, 0);
    }
}

@keyframes glitch-anim2 {
    0% {
        clip: rect(65px, 9999px, 119px, 0);
    }

    10% {
        clip: rect(79px, 9999px, 66px, 0);
    }

    20% {
        clip: rect(75px, 9999px, 87px, 0);
    }

    30% {
        clip: rect(101px, 9999px, 114px, 0);
    }

    40% {
        clip: rect(48px, 9999px, 128px, 0);
    }

    50% {
        clip: rect(68px, 9999px, 109px, 0);
    }

    60% {
        clip: rect(87px, 9999px, 129px, 0);
    }

    70% {
        clip: rect(53px, 9999px, 68px, 0);
    }

    80% {
        clip: rect(127px, 9999px, 74px, 0);
    }

    90% {
        clip: rect(33px, 9999px, 52px, 0);
    }

    100% {
        clip: rect(120px, 9999px, 100px, 0);
    }
}

@keyframes pulse {

    0%,
    100% {
        opacity: 0.5;
        transform: translate(-50%, -50%) scale(1);
    }

    50% {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1.1);
    }
}

@keyframes float {
    0% {
        transform: translateY(0) translateX(0);
        opacity: 1;
    }

    50% {
        opacity: 0.7;
    }

    100% {
        transform: translateY(-100vh) translateX(20px);
        opacity: 0;
    }
}
</style>