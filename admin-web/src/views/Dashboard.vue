<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <n-grid :cols="4" :x-gap="16" :y-gap="16" class="stats-grid">
      <n-gi>
        <n-card class="stat-card card-1" hoverable>
          <div class="stat-content">
            <div class="stat-icon">
              <n-icon :component="DocumentTextOutline" size="40" />
            </div>
            <div class="stat-info">
              <div class="stat-label">文章总数</div>
              <div class="stat-value">{{ stats.articleCount }}</div>
              <div class="stat-trend">
                <n-icon :component="TrendingUpOutline" size="16" />
                <span>较上月 +12%</span>
              </div>
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card card-2" hoverable>
          <div class="stat-content">
            <div class="stat-icon">
              <n-icon :component="PeopleOutline" size="40" />
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.userCount }}</div>
              <div class="stat-trend">
                <n-icon :component="TrendingUpOutline" size="16" />
                <span>较上月 +8%</span>
              </div>
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card card-3" hoverable>
          <div class="stat-content">
            <div class="stat-icon">
              <n-icon :component="ChatbubblesOutline" size="40" />
            </div>
            <div class="stat-info">
              <div class="stat-label">评论总数</div>
              <div class="stat-value">{{ stats.commentCount }}</div>
              <div class="stat-trend">
                <n-icon :component="TrendingUpOutline" size="16" />
                <span>较上月 +15%</span>
              </div>
            </div>
          </div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card class="stat-card card-4" hoverable>
          <div class="stat-content">
            <div class="stat-icon">
              <n-icon :component="EyeOutline" size="40" />
            </div>
            <div class="stat-info">
              <div class="stat-label">浏览总数</div>
              <div class="stat-value">{{ stats.viewCount }}</div>
              <div class="stat-trend">
                <n-icon :component="TrendingUpOutline" size="16" />
                <span>较上月 +23%</span>
              </div>
            </div>
          </div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 图表区域 -->
    <n-grid :cols="2" :x-gap="16" :y-gap="16" style="margin-top: 16px">
      <n-gi>
        <n-card title="访问趋势" class="chart-card" hoverable>
          <div ref="visitChartRef" style="width: 100%; height: 300px"></div>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="文章分类统计" class="chart-card" hoverable>
          <div ref="categoryChartRef" style="width: 100%; height: 300px"></div>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 技术栈和更新日志 -->
    <n-grid :cols="2" :x-gap="16" :y-gap="16" style="margin-top: 16px">
      <n-gi>
        <n-card title="技术栈" class="tech-card" hoverable>
          <template #header-extra>
            <n-tag type="success" size="small">v1.0.0</n-tag>
          </template>
          <n-space vertical :size="16">
            <div class="tech-section">
              <div class="tech-title">
                <n-icon :component="ServerOutline" color="#2ADB5C" />
                <span>后端技术</span>
              </div>
              <n-space :size="8" class="tech-tags">
                <n-tag type="info">Spring Boot 4.0.0-M2</n-tag>
                <n-tag type="info">MyBatis-Plus 3.5.14</n-tag>
                <n-tag type="info">Sa-Token 1.39.0</n-tag>
                <n-tag type="info">MySQL</n-tag>
                <n-tag type="info">Redis</n-tag>
                <n-tag type="info">Druid</n-tag>
              </n-space>
            </div>
            <n-divider style="margin: 8px 0" />
            <div class="tech-section">
              <div class="tech-title">
                <n-icon :component="CodeSlashOutline" color="#2ADB5C" />
                <span>前端技术</span>
              </div>
              <n-space :size="8" class="tech-tags">
                <n-tag type="success">Vue 3.5</n-tag>
                <n-tag type="success">Vite 7.2</n-tag>
                <n-tag type="success">Naive UI 2.38</n-tag>
                <n-tag type="success">Pinia 2.1</n-tag>
                <n-tag type="success">Vue Router 4.2</n-tag>
                <n-tag type="success">Echarts 5.4</n-tag>
              </n-space>
            </div>
            <n-divider style="margin: 8px 0" />
            <div class="tech-section">
              <div class="tech-title">
                <n-icon :component="CloudUploadOutline" color="#2ADB5C" />
                <span>存储方案</span>
              </div>
              <n-space :size="8" class="tech-tags">
                <n-tag type="warning">MinIO</n-tag>
                <n-tag type="warning">阿里云OSS</n-tag>
              </n-space>
            </div>
          </n-space>
        </n-card>
      </n-gi>
      <n-gi>
        <n-card title="更新日志" class="changelog-card" hoverable>
          <template #header-extra>
            <n-tag type="info" size="small">Latest</n-tag>
          </template>
          <n-timeline>
            <n-timeline-item
              type="success"
              title="v1.0.0"
              time="2025-11-27"
              :line-type="'dashed'"
            >
              <template #icon>
                <n-icon :component="CheckmarkCircleOutline" />
              </template>
              <ul class="changelog-list">
                <li>✨ 完成后台管理系统初始化</li>
                <li>🎨 实现黑色主题登录页面</li>
                <li>📝 完成文章、分类、标签管理</li>
                <li>🖼️ 集成MinIO/OSS文件上传</li>
                <li>📊 添加数据统计和图表展示</li>
              </ul>
            </n-timeline-item>
            <n-timeline-item
              type="warning"
              title="v0.9.0"
              time="2025-11-20"
              :line-type="'dashed'"
            >
              <template #icon>
                <n-icon :component="BuildOutline" />
              </template>
              <ul class="changelog-list">
                <li>🔧 项目架构搭建</li>
                <li>🔐 用户认证系统集成</li>
                <li>💾 数据库设计完成</li>
              </ul>
            </n-timeline-item>
            <n-timeline-item
              type="info"
              title="v0.5.0"
              time="2025-11-15"
            >
              <template #icon>
                <n-icon :component="RocketOutline" />
              </template>
              <ul class="changelog-list">
                <li>🚀 项目初始化</li>
                <li>📦 技术栈选型</li>
              </ul>
            </n-timeline-item>
          </n-timeline>
        </n-card>
      </n-gi>
    </n-grid>

    <!-- 系统信息 -->
    <n-card title="系统信息" class="system-card" style="margin-top: 16px" hoverable>
      <n-descriptions label-placement="left" :column="3" bordered>
        <n-descriptions-item label="当前用户">
          <n-tag type="success">
            <template #icon>
              <n-icon :component="PersonOutline" />
            </template>
            {{ userStore.userInfo.nickname || '管理员' }}
          </n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="登录时间">
          {{ currentTime }}
        </n-descriptions-item>
        <n-descriptions-item label="系统版本">
          <n-tag type="info">v1.0.0</n-tag>
        </n-descriptions-item>
        <n-descriptions-item label="运行环境">
          Java 25 + Node.js
        </n-descriptions-item>
        <n-descriptions-item label="数据库">
          MySQL 8.0 + Redis
        </n-descriptions-item>
        <n-descriptions-item label="系统状态">
          <n-tag type="success">
            <template #icon>
              <n-icon :component="CheckmarkCircleOutline" />
            </template>
            运行正常
          </n-tag>
        </n-descriptions-item>
      </n-descriptions>
    </n-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import {
  DocumentTextOutline,
  PeopleOutline,
  ChatbubblesOutline,
  EyeOutline,
  TrendingUpOutline,
  ServerOutline,
  CodeSlashOutline,
  CloudUploadOutline,
  CheckmarkCircleOutline,
  BuildOutline,
  RocketOutline,
  PersonOutline
} from '@vicons/ionicons5'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const visitChartRef = ref(null)
const categoryChartRef = ref(null)
let visitChart = null
let categoryChart = null

const stats = ref({
  articleCount: 156,
  userCount: 1234,
  commentCount: 589,
  viewCount: 12345
})

const currentTime = ref(new Date().toLocaleString('zh-CN'))

// 初始化访问趋势图表
const initVisitChart = () => {
  visitChart = echarts.init(visitChartRef.value)
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#e5e7eb'
        }
      },
      axisLabel: {
        color: '#6b7280'
      },
      splitLine: {
        lineStyle: {
          color: '#f3f4f6'
        }
      }
    },
    series: [
      {
        name: '访问量',
        type: 'bar',
        data: [320, 432, 301, 534, 590, 530, 620],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2ADB5C' },
            { offset: 1, color: '#18A058' }
          ]),
          borderRadius: [4, 4, 0, 0]
        },
        emphasis: {
          itemStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: '#36f567' },
              { offset: 1, color: '#2ADB5C' }
            ])
          }
        }
      }
    ]
  }
  visitChart.setOption(option)
}

// 初始化分类统计图表
const initCategoryChart = () => {
  categoryChart = echarts.init(categoryChartRef.value)
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '10%',
      top: 'center',
      textStyle: {
        color: '#6b7280'
      }
    },
    series: [
      {
        name: '分类统计',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#ffffff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold',
            color: '#2ADB5C'
          }
        },
        labelLine: {
          show: false
        },
        data: [
          { value: 45, name: 'Java', itemStyle: { color: '#2ADB5C' } },
          { value: 28, name: 'Spring', itemStyle: { color: '#18A058' } },
          { value: 35, name: '前端', itemStyle: { color: '#36ad6a' } },
          { value: 22, name: '数据库', itemStyle: { color: '#0c7a43' } },
          { value: 26, name: '算法', itemStyle: { color: '#4ade80' } }
        ]
      }
    ]
  }
  categoryChart.setOption(option)
}

// 窗口大小改变时重新渲染图表
const handleResize = () => {
  visitChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  initVisitChart()
  initCategoryChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  visitChart?.dispose()
  categoryChart?.dispose()
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.dashboard {
  max-width: 1600px;
}

/* 统计卡片样式 */
.stats-grid {
  margin-bottom: 16px;
}

.stat-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  transition: all 0.3s ease;
  background: #ffffff;
}

.stat-card:hover {
  border-color: #2ADB5C;
  box-shadow: 0 8px 24px rgba(42, 219, 92, 0.15);
  transform: translateY(-2px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.card-1 .stat-icon {
  background: linear-gradient(135deg, rgba(42, 219, 92, 0.1) 0%, rgba(42, 219, 92, 0.05) 100%);
  color: #2ADB5C;
}

.card-2 .stat-icon {
  background: linear-gradient(135deg, rgba(24, 160, 88, 0.1) 0%, rgba(24, 160, 88, 0.05) 100%);
  color: #18A058;
}

.card-3 .stat-icon {
  background: linear-gradient(135deg, rgba(54, 173, 106, 0.1) 0%, rgba(54, 173, 106, 0.05) 100%);
  color: #36ad6a;
}

.card-4 .stat-icon {
  background: linear-gradient(135deg, rgba(74, 222, 128, 0.1) 0%, rgba(74, 222, 128, 0.05) 100%);
  color: #4ade80;
}

.stat-card:hover .stat-icon {
  transform: scale(1.1) rotate(5deg);
}

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #2ADB5C;
  margin-bottom: 4px;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #18A058;
}

/* 图表卡片样式 */
.chart-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  transition: all 0.3s ease;
}

.chart-card:hover {
  border-color: #2ADB5C;
  box-shadow: 0 4px 16px rgba(42, 219, 92, 0.1);
}

/* 技术栈卡片样式 */
.tech-card,
.changelog-card,
.system-card {
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: #ffffff;
  transition: all 0.3s ease;
}

.tech-card:hover,
.changelog-card:hover,
.system-card:hover {
  border-color: #2ADB5C;
  box-shadow: 0 4px 16px rgba(42, 219, 92, 0.1);
}

.tech-section {
  padding: 8px 0;
}

.tech-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.tech-tags {
  flex-wrap: wrap;
}

/* 更新日志样式 */
.changelog-list {
  margin: 8px 0;
  padding-left: 20px;
  color: #6b7280;
  font-size: 13px;
  line-height: 1.8;
}

.changelog-list li {
  margin: 4px 0;
}
</style>
