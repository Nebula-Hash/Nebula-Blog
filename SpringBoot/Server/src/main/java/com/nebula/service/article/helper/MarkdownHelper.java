package com.nebula.service.article.helper;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.task.list.items.TaskListItemsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Markdown 解析工具类
 * <p>
 * 封装 Markdown 转 HTML 的逻辑，支持以下格式：
 * <h3>核心格式（commonmark 默认支持）</h3>
 * <ul>
 *   <li>标题：# H1 至 ###### H6</li>
 *   <li>段落：空行分隔</li>
 *   <li>换行：行末两空格</li>
 *   <li>加粗：**bold** 或 __bold__</li>
 *   <li>斜体：*italic* 或 _italic_</li>
 *   <li>粗斜体：***text***</li>
 *   <li>行内代码：`code`</li>
 *   <li>代码块：``` 或缩进4空格</li>
 *   <li>引用：> quote</li>
 *   <li>无序列表：- item 或 * item</li>
 *   <li>有序列表：1. item</li>
 *   <li>链接：[text](url)</li>
 *   <li>图片：![alt](url)</li>
 *   <li>水平线：--- 或 ***</li>
 * </ul>
 * <h3>扩展格式（已启用插件）</h3>
 * <ul>
 *   <li>GFM 表格：| col1 | col2 |</li>
 *   <li>GFM 删除线：~~strikethrough~~</li>
 *   <li>任务列表：- [ ] todo / - [x] done</li>
 *   <li>自动链接：自动识别 URL（如 <a href="https://example.com">...</a>）</li>
 *   <li>标题锚点：为标题生成 id 属性，支持目录跳转</li>
 * </ul>
 *
 * @author Nebula-Hash
 * @date 2026/1/31
 */
@Component
public class MarkdownHelper {

    private final Parser parser;
    private final HtmlRenderer renderer;

    public MarkdownHelper() {
        // 启用所有扩展
        List<Extension> extensions = List.of(
                TablesExtension.create(),           // GFM 表格
                StrikethroughExtension.create(),    // GFM 删除线
                TaskListItemsExtension.create(),    // 任务列表
                AutolinkExtension.create(),         // 自动链接
                HeadingAnchorExtension.create()     // 标题锚点
        );

        this.parser = Parser.builder()
                .extensions(extensions)
                .build();

        this.renderer = HtmlRenderer.builder()
                .extensions(extensions)
                .build();
    }

    /**
     * 将 Markdown 内容转换为 HTML
     *
     * @param markdown Markdown 格式的内容
     * @return HTML 格式的内容，若输入为空则返回空字符串
     */
    public String toHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }
        Node document = parser.parse(markdown);
        return renderer.render(document);
    }
}
