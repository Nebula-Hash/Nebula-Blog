<template>
  <div class="markdown-renderer prose" v-html="renderedHtml"></div>
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
const defaultLinkRender = md.renderer.rules.link_open || function (tokens, idx, options, env, self) {
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
  width: 100%;
}
</style>
