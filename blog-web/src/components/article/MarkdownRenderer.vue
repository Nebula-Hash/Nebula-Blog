<template>
  <div class="markdown-renderer" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/atom-one-dark.css'

const props = defineProps({
  content: {
    type: String,
    required: true
  },
  // 是否启用 HTML 标签（默认禁用以防止 XSS）
  html: {
    type: Boolean,
    default: false
  },
  // 是否自动转换链接
  linkify: {
    type: Boolean,
    default: true
  },
  // 是否启用排版优化
  typographer: {
    type: Boolean,
    default: true
  }
})

// 配置 Markdown-it
const md = new MarkdownIt({
  html: props.html,
  linkify: props.linkify,
  typographer: props.typographer,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return `<pre class="hljs"><code>${hljs.highlight(str, { language: lang, ignoreIllegals: true }).value}</code></pre>`
      } catch (error) {
        console.error('代码高亮失败:', error)
      }
    }
    return `<pre class="hljs"><code>${md.utils.escapeHtml(str)}</code></pre>`
  }
})

// 自定义链接渲染规则（添加安全属性）
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

// 渲染 Markdown
const renderedHtml = computed(() => {
  if (!props.content) return ''
  
  try {
    return md.render(props.content)
  } catch (error) {
    console.error('Markdown 渲染失败:', error)
    return '<p>内容渲染失败</p>'
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
  line-height: 1.8;
}

/* 链接样式 */
.markdown-renderer :deep(a) {
  color: #2ADB5C;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: all 0.3s;
}

.markdown-renderer :deep(a:hover) {
  border-bottom-color: #2ADB5C;
}

/* 列表样式 */
.markdown-renderer :deep(ul),
.markdown-renderer :deep(ol) {
  margin: 16px 0;
  padding-left: 32px;
}

.markdown-renderer :deep(li) {
  margin: 8px 0;
  line-height: 1.8;
}

/* 引用样式 */
.markdown-renderer :deep(blockquote) {
  margin: 16px 0;
  padding: 12px 20px;
  border-left: 4px solid #2ADB5C;
  background: rgba(42, 219, 92, 0.05);
  border-radius: 4px;
  color: rgba(255, 255, 255, 0.75);
}

.markdown-renderer :deep(blockquote p) {
  margin: 0;
}

/* 代码块样式 */
.markdown-renderer :deep(pre) {
  margin: 20px 0;
  padding: 16px;
  background: #1F1F1F;
  border-radius: 8px;
  overflow-x: auto;
  border-left: 3px solid #2ADB5C;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.markdown-renderer :deep(pre code) {
  background: transparent;
  padding: 0;
  border-radius: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #e6e6e6;
}

/* 行内代码样式 */
.markdown-renderer :deep(code) {
  background: #1F1F1F;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 14px;
  color: #2ADB5C;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
}

/* 表格样式 */
.markdown-renderer :deep(table) {
  margin: 20px 0;
  border-collapse: collapse;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.markdown-renderer :deep(th),
.markdown-renderer :deep(td) {
  padding: 12px 16px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  text-align: left;
}

.markdown-renderer :deep(th) {
  background: rgba(42, 219, 92, 0.1);
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
}

.markdown-renderer :deep(tr:nth-child(even)) {
  background: rgba(255, 255, 255, 0.02);
}

.markdown-renderer :deep(tr:hover) {
  background: rgba(42, 219, 92, 0.05);
}

/* 图片样式 */
.markdown-renderer :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 20px 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

/* 分割线样式 */
.markdown-renderer :deep(hr) {
  margin: 24px 0;
  border: none;
  border-top: 2px solid rgba(42, 219, 92, 0.2);
}

/* 强调样式 */
.markdown-renderer :deep(strong) {
  font-weight: 600;
  color: rgba(255, 255, 255, 0.95);
}

.markdown-renderer :deep(em) {
  font-style: italic;
  color: rgba(255, 255, 255, 0.85);
}

/* 删除线样式 */
.markdown-renderer :deep(del) {
  text-decoration: line-through;
  color: rgba(255, 255, 255, 0.5);
}
</style>
