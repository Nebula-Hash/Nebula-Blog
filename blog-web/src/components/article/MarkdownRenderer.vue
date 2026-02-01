<template>
  <div class="markdown-renderer">
    <!-- 分段渲染 -->
    <div 
      v-for="(chunk, index) in renderedChunks" 
      :key="index"
      class="markdown-chunk"
      v-html="chunk"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'

const props = defineProps({
  content: {
    type: String,
    required: true
  },
  chunkSize: {
    type: Number,
    default: 10000 // 超过10000字符时分段渲染
  },
  enableLazyImages: {
    type: Boolean,
    default: true
  }
})

const renderedChunks = ref([])

// 配置 markdown-it
const md = new MarkdownIt({
  html: false, // 不允许HTML标签（安全考虑）
  linkify: true, // 自动识别链接
  typographer: true, // 优化排版
  highlight: function (str, lang) {
    // 代码高亮
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      } catch (err) {
        console.error('Highlight error:', err)
      }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

// 自定义渲染规则
const defaultImageRender = md.renderer.rules.image || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}

// 图片懒加载
md.renderer.rules.image = function (tokens, idx, options, env, self) {
  const token = tokens[idx]
  const srcIndex = token.attrIndex('src')
  const src = token.attrs[srcIndex][1]
  const alt = token.content || ''
  
  if (props.enableLazyImages) {
    // 使用懒加载
    return `<img class="lazy-image" data-src="${src}" alt="${alt}" loading="lazy" />`
  }
  
  return defaultImageRender(tokens, idx, options, env, self)
}

// 外部链接添加安全属性
const defaultLinkRender = md.renderer.rules.link_open || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.link_open = function (tokens, idx, options, env, self) {
  const token = tokens[idx]
  const hrefIndex = token.attrIndex('href')
  
  if (hrefIndex >= 0) {
    const href = token.attrs[hrefIndex][1]
    
    // 外部链接添加安全属性
    if (href.startsWith('http://') || href.startsWith('https://')) {
      token.attrPush(['target', '_blank'])
      token.attrPush(['rel', 'noopener noreferrer'])
    }
  }
  
  return defaultLinkRender(tokens, idx, options, env, self)
}

// 表格添加响应式样式
const defaultTableRender = md.renderer.rules.table_open || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.table_open = function (tokens, idx, options, env, self) {
  return '<div class="table-wrapper">' + defaultTableRender(tokens, idx, options, env, self)
}

const defaultTableCloseRender = md.renderer.rules.table_close || function(tokens, idx, options, env, self) {
  return self.renderToken(tokens, idx, options)
}

md.renderer.rules.table_close = function (tokens, idx, options, env, self) {
  return defaultTableCloseRender(tokens, idx, options, env, self) + '</div>'
}

// 渲染Markdown
const renderMarkdown = () => {
  if (!props.content) {
    renderedChunks.value = []
    return
  }
  
  const content = props.content
  
  // 如果内容较短，直接渲染
  if (content.length <= props.chunkSize) {
    renderedChunks.value = [md.render(content)]
    return
  }
  
  // 长文本分段渲染
  const chunks = []
  const lines = content.split('\n')
  let currentChunk = []
  let currentLength = 0
  
  for (const line of lines) {
    currentChunk.push(line)
    currentLength += line.length
    
    if (currentLength >= props.chunkSize) {
      chunks.push(currentChunk.join('\n'))
      currentChunk = []
      currentLength = 0
    }
  }
  
  // 添加剩余内容
  if (currentChunk.length > 0) {
    chunks.push(currentChunk.join('\n'))
  }
  
  // 渲染每个分段
  renderedChunks.value = chunks.map(chunk => md.render(chunk))
}

// 监听内容变化
watch(() => props.content, () => {
  renderMarkdown()
}, { immediate: true })

// 初始化懒加载图片
onMounted(() => {
  if (props.enableLazyImages) {
    // 使用 Intersection Observer 实现图片懒加载
    const images = document.querySelectorAll('.markdown-renderer img.lazy-image')
    
    if ('IntersectionObserver' in window) {
      const imageObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target
            const src = img.getAttribute('data-src')
            if (src) {
              img.src = src
              img.removeAttribute('data-src')
              img.classList.remove('lazy-image')
              imageObserver.unobserve(img)
            }
          }
        })
      }, {
        rootMargin: '50px'
      })
      
      images.forEach(img => imageObserver.observe(img))
    } else {
      // 不支持 Intersection Observer，直接加载所有图片
      images.forEach(img => {
        const src = img.getAttribute('data-src')
        if (src) {
          img.src = src
          img.removeAttribute('data-src')
        }
      })
    }
  }
})
</script>

<style scoped>
.markdown-renderer {
  line-height: 1.8;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  word-wrap: break-word;
}

.markdown-chunk {
  margin-bottom: 20px;
}

/* 标题样式 */
.markdown-renderer :deep(h1),
.markdown-renderer :deep(h2),
.markdown-renderer :deep(h3),
.markdown-renderer :deep(h4),
.markdown-renderer :deep(h5),
.markdown-renderer :deep(h6) {
  margin: 24px 0 16px;
  font-weight: 600;
  line-height: 1.4;
  color: rgba(255, 255, 255, 0.95);
}

.markdown-renderer :deep(h1) {
  font-size: 32px;
  border-bottom: 2px solid rgba(42, 219, 92, 0.3);
  padding-bottom: 8px;
}

.markdown-renderer :deep(h2) {
  font-size: 28px;
  border-bottom: 1px solid rgba(42, 219, 92, 0.2);
  padding-bottom: 6px;
}

.markdown-renderer :deep(h3) {
  font-size: 24px;
}

.markdown-renderer :deep(h4) {
  font-size: 20px;
}

/* 段落样式 */
.markdown-renderer :deep(p) {
  margin: 16px 0;
}

/* 链接样式 */
.markdown-renderer :deep(a) {
  color: #2ADB5C;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.3s;
}

.markdown-renderer :deep(a:hover) {
  border-bottom-color: #2ADB5C;
}

/* 图片样式 */
.markdown-renderer :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px 0;
  display: block;
}

.markdown-renderer :deep(img.lazy-image) {
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s ease-in-out infinite;
  min-height: 200px;
}

/* 代码块样式 */
.markdown-renderer :deep(pre) {
  background: #1F1F1F;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  border-left: 3px solid #2ADB5C;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  margin: 20px 0;
}

.markdown-renderer :deep(code) {
  background: rgba(42, 219, 92, 0.1);
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
  color: #2ADB5C;
  font-family: 'Courier New', Consolas, Monaco, monospace;
}

.markdown-renderer :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}

/* 引用样式 */
.markdown-renderer :deep(blockquote) {
  margin: 20px 0;
  padding: 12px 20px;
  border-left: 4px solid #2ADB5C;
  background: rgba(42, 219, 92, 0.05);
  color: rgba(255, 255, 255, 0.75);
}

/* 列表样式 */
.markdown-renderer :deep(ul),
.markdown-renderer :deep(ol) {
  margin: 16px 0;
  padding-left: 28px;
}

.markdown-renderer :deep(li) {
  margin: 8px 0;
}

/* 表格样式 */
.markdown-renderer :deep(.table-wrapper) {
  overflow-x: auto;
  margin: 20px 0;
}

.markdown-renderer :deep(table) {
  width: 100%;
  border-collapse: collapse;
  border-spacing: 0;
}

.markdown-renderer :deep(th),
.markdown-renderer :deep(td) {
  padding: 12px;
  border: 1px solid rgba(150, 150, 150, 0.2);
  text-align: left;
}

.markdown-renderer :deep(th) {
  background: rgba(42, 219, 92, 0.1);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
}

.markdown-renderer :deep(tr:nth-child(even)) {
  background: rgba(150, 150, 150, 0.05);
}

/* 分隔线样式 */
.markdown-renderer :deep(hr) {
  margin: 24px 0;
  border: none;
  border-top: 1px solid rgba(150, 150, 150, 0.2);
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
