package com.example.hexobloguploader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.hexobloguploader.databinding.ActivityBlogDetailBinding
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BlogDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlogDetailBinding
    private var currentBlogId: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlogDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        loadBlogData()
        setupButtons()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // 设置菜单
        binding.toolbar.inflateMenu(R.menu.menu_blog_detail)
        binding.toolbar.setOnMenuItemClickListener { menuItem: android.view.MenuItem ->
            when (menuItem.itemId) {
                R.id.menu_delete -> {
                    deleteBlog()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadBlogData() {
        // 从 Intent 获取博客数据
        val bundle = intent.getBundleExtra("BLOG_DATA")
        
        if (bundle != null) {
            val id = bundle.getString("id", "")
            val title = bundle.getString("title", "未命名博客")
            val content = bundle.getString("content", "")
            val date = bundle.getString("date", "")
            val tags = bundle.getStringArrayList("tags") ?: emptyList<String>()
            val filePath = bundle.getString("filePath", "")
            
            supportActionBar?.title = title
            binding.textBlogTitle.text = title
            binding.textBlogDate.text = "发布日期: $date"
            binding.textBlogContent.text = content
            
            // 显示标签
            binding.textBlogTags.text = "标签: ${tags.joinToString(", ")}"
            binding.textFilePath.text = "文件路径: $filePath"
            
            // 保存 id 到实例变量以备后用
            this.currentBlogId = id
        } else {
            // 如果没有数据，显示示例
            supportActionBar?.title = "博客详情"
            binding.textBlogTitle.text = "示例博客"
            binding.textBlogDate.text = "发布日期: 2025-03-25"
            binding.textBlogContent.text = "这是博客的详细内容。\n\n你可以在这里编辑和预览博客文章。\n\n支持 Markdown 格式。"
            binding.textBlogTags.text = "标签: 示例, 测试"
            binding.textFilePath.text = "文件路径: /sample/blog.md"
        }
    }
    
    private fun setupButtons() {
        binding.buttonEdit.setOnClickListener {
            // 编辑博客
            showEditBlog()
        }
        
        binding.buttonPreview.setOnClickListener {
            // 预览博客
            showPreview()
        }
        
        binding.buttonUpload.setOnClickListener {
            // 上传博客
            uploadBlog()
        }
        
        binding.fabSave.setOnClickListener {
            // 保存博客
            saveBlog()
        }
    }
    
    private fun showEditBlog() {
        // 从 Intent 获取博客数据
        val bundle = intent.getBundleExtra("BLOG_DATA")
        
        if (bundle != null) {
            val id = bundle.getString("id", "")
            val title = bundle.getString("title", "未命名博客")
            val content = bundle.getString("content", "")
            val date = bundle.getString("date", "")
            val tags = bundle.getStringArrayList("tags") ?: emptyList<String>()
            val filePath = bundle.getString("filePath", "")
            
            // 从文件路径提取文件名
            val fileName = if (filePath.isNotEmpty()) {
                File(filePath).name
            } else {
                "${date}-${title.replace(" ", "-").replace("[^a-zA-Z0-9-]".toRegex(), "").lowercase()}.md"
            }
            
            // 创建 Post 对象
            val post = com.example.hexobloguploader.model.Post(
                id = id,
                title = title,
                fileName = fileName,
                content = content,
                bodyContent = content, // 使用相同的内容作为正文
                date = date,
                categories = listOf("未分类"), // 暂时使用默认分类
                tags = tags,
                filePath = filePath,
                lastModified = System.currentTimeMillis()
            )
            
            // 打开 EditPostActivity 进行编辑
            val intent = android.content.Intent(this, EditPostActivity::class.java).apply {
                putExtra("POST_DATA", post)
            }
            startActivity(intent)
        } else {
            android.widget.Toast.makeText(
                this,
                "无法获取博客数据",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun showPreview() {
        // 从 Intent 获取博客数据
        val bundle = intent.getBundleExtra("BLOG_DATA")
        
        if (bundle != null) {
            val content = bundle.getString("content", "")
            
            // 显示加载对话框
            val loadingDialog = android.app.AlertDialog.Builder(this)
                .setTitle("Markdown 预览")
                .setMessage("正在渲染 Markdown...")
                .setCancelable(false)
                .create()
            
            loadingDialog.show()
            
            // 在后台线程处理 Markdown 渲染
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // 初始化 Markwon
                    val markwon = Markwon.create(this@BlogDetailActivity)
                    
                    // 渲染 Markdown 为 HTML
                    // 使用 Markwon 的 render 方法，然后转换为 HTML
                    val spanned = markwon.toMarkdown(content)
                    val htmlContent = android.text.Html.toHtml(spanned)
                    
                    // 创建完整的 HTML 文档
                    val fullHtml = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta charset="UTF-8">
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>
                                body {
                                    font-family: 'Roboto', sans-serif;
                                    line-height: 1.6;
                                    color: #333;
                                    padding: 20px;
                                    max-width: 800px;
                                    margin: 0 auto;
                                }
                                h1, h2, h3 {
                                    color: #2c3e50;
                                    margin-top: 1.5em;
                                    margin-bottom: 0.5em;
                                }
                                h1 { font-size: 2em; }
                                h2 { font-size: 1.5em; }
                                h3 { font-size: 1.2em; }
                                p {
                                    margin-bottom: 1em;
                                }
                                code {
                                    background-color: #f5f5f5;
                                    padding: 2px 4px;
                                    border-radius: 3px;
                                    font-family: 'Courier New', monospace;
                                }
                                pre {
                                    background-color: #f5f5f5;
                                    padding: 10px;
                                    border-radius: 5px;
                                    overflow-x: auto;
                                }
                                pre code {
                                    background-color: transparent;
                                    padding: 0;
                                }
                                a {
                                    color: #3498db;
                                    text-decoration: none;
                                }
                                a:hover {
                                    text-decoration: underline;
                                }
                                img {
                                    max-width: 100%;
                                    height: auto;
                                }
                                ul, ol {
                                    padding-left: 20px;
                                    margin-bottom: 1em;
                                }
                                li {
                                    margin-bottom: 0.5em;
                                }
                                blockquote {
                                    border-left: 4px solid #3498db;
                                    padding-left: 15px;
                                    margin-left: 0;
                                    color: #666;
                                    font-style: italic;
                                }
                            </style>
                        </head>
                        <body>
                            $htmlContent
                        </body>
                        </html>
                    """.trimIndent()
                    
                    // 切换到主线程显示结果
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        
                        // 创建预览对话框
                        val dialog = android.app.AlertDialog.Builder(this@BlogDetailActivity)
                            .setTitle("Markdown 预览")
                            .setPositiveButton("关闭", null)
                            .create()
                        
                        // 创建 WebView 用于显示渲染后的 Markdown
                        val webView = android.webkit.WebView(this@BlogDetailActivity)
                        webView.settings.javaScriptEnabled = true
                        
                        // 添加加载监听器
                        webView.webViewClient = object : android.webkit.WebViewClient() {
                            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                // 页面加载完成后的逻辑
                            }
                        }
                        
                        // 加载 HTML 内容
                        webView.loadDataWithBaseURL(
                            null,
                            fullHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                        
                        // 设置对话框视图为WebView
                        dialog.setView(webView)
                        dialog.show()
                        
                        // 设置对话框大小
                        val window = dialog.window
                        if (window != null) {
                            val layoutParams = android.view.WindowManager.LayoutParams()
                            layoutParams.copyFrom(window.attributes)
                            layoutParams.width = android.view.WindowManager.LayoutParams.MATCH_PARENT
                            layoutParams.height = android.view.WindowManager.LayoutParams.MATCH_PARENT
                            window.attributes = layoutParams
                        }
                    }
                } catch (e: Exception) {
                    // 处理异常
                    withContext(Dispatchers.Main) {
                        loadingDialog.dismiss()
                        android.widget.Toast.makeText(
                            this@BlogDetailActivity,
                            "渲染失败: ${e.message}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            android.widget.Toast.makeText(
                this,
                "无法获取博客内容",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    /**
     * 将 Markdown 转换为 HTML
     * 优化版本：使用更高效的正则表达式，避免性能问题
     */
    private fun convertMarkdownToHtml(markdown: String): String {
        // 对于大型文档，使用更高效的处理方式
        // 限制处理长度，避免性能问题
        val processedMarkdown = if (markdown.length > 10000) {
            markdown.substring(0, 10000) + "\n\n...（内容过长，已截断）"
        } else {
            markdown
        }
        
        // 使用 StringBuilder 提高性能
        val htmlBuilder = StringBuilder()
        
        // 添加 HTML 头部和样式
        htmlBuilder.append("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: 'Roboto', sans-serif;
                        line-height: 1.6;
                        color: #333;
                        padding: 20px;
                        max-width: 800px;
                        margin: 0 auto;
                    }
                    h1, h2, h3 {
                        color: #2c3e50;
                        margin-top: 1.5em;
                        margin-bottom: 0.5em;
                    }
                    h1 { font-size: 2em; }
                    h2 { font-size: 1.5em; }
                    h3 { font-size: 1.2em; }
                    p {
                        margin-bottom: 1em;
                    }
                    code {
                        background-color: #f5f5f5;
                        padding: 2px 4px;
                        border-radius: 3px;
                        font-family: 'Courier New', monospace;
                    }
                    pre {
                        background-color: #f5f5f5;
                        padding: 10px;
                        border-radius: 5px;
                        overflow-x: auto;
                    }
                    pre code {
                        background-color: transparent;
                        padding: 0;
                    }
                    a {
                        color: #3498db;
                        text-decoration: none;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    img {
                        max-width: 100%;
                        height: auto;
                    }
                    ul, ol {
                        padding-left: 20px;
                        margin-bottom: 1em;
                    }
                    li {
                        margin-bottom: 0.5em;
                    }
                    blockquote {
                        border-left: 4px solid #3498db;
                        padding-left: 15px;
                        margin-left: 0;
                        color: #666;
                        font-style: italic;
                    }
                </style>
            </head>
            <body>
        """.trimIndent())
        
        // 简单的 Markdown 转换（优化性能）
        val lines = processedMarkdown.lines()
        var inCodeBlock = false
        var inParagraph = false
        
        for (line in lines) {
            val trimmedLine = line.trim()
            
            if (trimmedLine.startsWith("```")) {
                // 代码块开始/结束
                inCodeBlock = !inCodeBlock
                if (inCodeBlock) {
                    htmlBuilder.append("<pre><code>")
                } else {
                    htmlBuilder.append("</code></pre>")
                }
                continue
            }
            
            if (inCodeBlock) {
                // 在代码块中，直接添加行
                htmlBuilder.append(escapeHtml(line)).append("\n")
                continue
            }
            
            if (trimmedLine.isEmpty()) {
                // 空行结束段落
                if (inParagraph) {
                    htmlBuilder.append("</p>")
                    inParagraph = false
                }
                continue
            }
            
            // 处理标题
            when {
                trimmedLine.startsWith("# ") -> {
                    if (inParagraph) {
                        htmlBuilder.append("</p>")
                        inParagraph = false
                    }
                    htmlBuilder.append("<h1>").append(escapeHtml(trimmedLine.substring(2))).append("</h1>")
                }
                trimmedLine.startsWith("## ") -> {
                    if (inParagraph) {
                        htmlBuilder.append("</p>")
                        inParagraph = false
                    }
                    htmlBuilder.append("<h2>").append(escapeHtml(trimmedLine.substring(3))).append("</h2>")
                }
                trimmedLine.startsWith("### ") -> {
                    if (inParagraph) {
                        htmlBuilder.append("</p>")
                        inParagraph = false
                    }
                    htmlBuilder.append("<h3>").append(escapeHtml(trimmedLine.substring(4))).append("</h3>")
                }
                trimmedLine.startsWith("* ") || trimmedLine.startsWith("- ") -> {
                    if (!inParagraph) {
                        htmlBuilder.append("<ul>")
                    }
                    htmlBuilder.append("<li>").append(processInlineMarkdown(trimmedLine.substring(2))).append("</li>")
                    inParagraph = false
                }
                else -> {
                    // 普通段落
                    if (!inParagraph) {
                        htmlBuilder.append("<p>")
                        inParagraph = true
                    } else {
                        htmlBuilder.append("<br>")
                    }
                    htmlBuilder.append(processInlineMarkdown(trimmedLine))
                }
            }
        }
        
        // 关闭最后一个段落
        if (inParagraph) {
            htmlBuilder.append("</p>")
        }
        
        // 关闭列表（如果有）
        htmlBuilder.append("</ul>")
        
        // 添加 HTML 尾部
        htmlBuilder.append("""
            </body>
            </html>
        """.trimIndent())
        
        return htmlBuilder.toString()
    }
    
    /**
     * 处理行内 Markdown 格式
     */
    private fun processInlineMarkdown(text: String): String {
        var result = text
        
        // 粗体：**text** 或 __text__
        result = result.replace("\\*\\*(.*?)\\*\\*".toRegex(), "<strong>$1</strong>")
        result = result.replace("__(.*?)__".toRegex(), "<strong>$1</strong>")
        
        // 斜体：*text* 或 _text_
        result = result.replace("\\*(.*?)\\*".toRegex(), "<em>$1</em>")
        result = result.replace("_(.*?)_".toRegex(), "<em>$1</em>")
        
        // 行内代码：`code`
        result = result.replace("`(.*?)`".toRegex(), "<code>$1</code>")
        
        // 链接：[text](url)
        result = result.replace("\\[(.*?)\\]\\((.*?)\\)".toRegex(), "<a href=\"$2\">$1</a>")
        
        // 图片：![alt](url)
        result = result.replace("!\\[(.*?)\\]\\((.*?)\\)".toRegex(), "<img src=\"$2\" alt=\"$1\">")
        
        return escapeHtml(result)
    }
    
    /**
     * 转义 HTML 特殊字符
     */
    private fun escapeHtml(text: String): String {
        return text
            .replace("&", "&")
            .replace("<", "<")
            .replace(">", ">")
            .replace("\"", """)
            .replace("'", "&#39;")
    }
    
    private fun uploadBlog() {
        // 后续将实现上传功能
        android.widget.Toast.makeText(
            this,
            "开始上传",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun saveBlog() {
        // 保存博客到本地
        android.widget.Toast.makeText(
            this,
            "保存博客",
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }
    
    /**
     * 删除博客
     */
    private fun deleteBlog() {
        // 从 Intent 获取博客数据
        val bundle = intent.getBundleExtra("BLOG_DATA")
        
        if (bundle != null) {
            val id = bundle.getString("id", "")
            val title = bundle.getString("title", "未命名博客")
            val filePath = bundle.getString("filePath", "")
            
            // 显示确认对话框
            android.app.AlertDialog.Builder(this)
                .setTitle("删除博客")
                .setMessage("确定要删除博客《$title》吗？此操作不可撤销。")
                .setPositiveButton("删除") { _, _ ->
                    // 创建 Post 对象用于删除
                    val post = com.example.hexobloguploader.model.Post(
                        id = id,
                        title = title,
                        fileName = File(filePath).name,
                        content = "",
                        bodyContent = "", // 添加空的正文内容
                        date = "",
                        categories = emptyList(),
                        tags = emptyList(),
                        filePath = filePath,
                        lastModified = System.currentTimeMillis()
                    )
                    
                    // 使用存储管理器删除博客
                    val storageManager = com.example.hexobloguploader.storage.BlogStorageManager(this)
                    val success = storageManager.deletePost(post)
                    
                    if (success) {
                        android.widget.Toast.makeText(
                            this,
                            "博客删除成功",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                        // 返回并刷新列表
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        android.widget.Toast.makeText(
                            this,
                            "博客删除失败",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            android.widget.Toast.makeText(
                this,
                "无法获取博客数据",
                android.widget.Toast.LENGTH_SHORT
            ).show()
        }
    }
}
