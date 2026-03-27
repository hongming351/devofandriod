package com.example.hexobloguploader.model

import com.example.hexobloguploader.utils.YamlParser
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Hexo 博客文章数据模型
 * 对应 Hexo 的 Markdown 文件结构
 */
data class Post(
    val id: String,
    val title: String,
    val fileName: String,          // Markdown 文件名，如 "2025-03-25-my-blog-post.md"
    val content: String,           // Markdown 内容（包含 Front-matter 和正文）
    val bodyContent: String,       // 纯正文内容（不包含 Front-matter）
    val date: String,              // 发布日期，格式：yyyy-MM-dd HH:mm:ss
    val categories: List<String>,  // 分类
    val tags: List<String>,        // 标签
    val filePath: String,          // 完整文件路径
    val lastModified: Long = System.currentTimeMillis(),
    val frontMatterData: Map<String, Any> = emptyMap()  // 完整的 Front-matter 数据
) : Serializable {
    companion object {
        /**
         * 从文件创建 Post 对象
         */
        fun fromFile(file: File): Post? {
            if (!file.exists() || !file.isFile || !file.name.endsWith(".md")) {
                return null
            }

            val content = file.readText()
            val fileName = file.name
            val id = file.absolutePath.hashCode().toString()
            
            // 从文件名提取日期（Hexo 标准格式：yyyy-MM-dd-title.md）
            val dateFromFileName = extractDateFromFileName(fileName)
            
            // 使用 YAML 解析器解析 Front-matter
            val (frontMatter, bodyContent) = YamlParser.parseFrontMatter(content)
            
            // 从 Front-matter 提取数据
            val title = YamlParser.getStringValue(frontMatter, "title", "未命名文章")
            val categories = YamlParser.getStringList(frontMatter, "categories")
            val tags = YamlParser.getStringList(frontMatter, "tags")
            
            // 获取日期（优先使用 Front-matter 中的日期）
            val date = if (frontMatter?.containsKey("date") == true) {
                val dateObj = YamlParser.getDateValue(frontMatter, "date")
                dateObj?.let { formatDate(it) } ?: dateFromFileName
            } else {
                dateFromFileName
            }
            
            return Post(
                id = id,
                title = title,
                fileName = fileName,
                content = content,
                bodyContent = bodyContent,
                date = date,
                categories = categories,
                tags = tags,
                filePath = file.absolutePath,
                lastModified = file.lastModified(),
                frontMatterData = frontMatter ?: emptyMap()
            )
        }
        
        /**
         * 从文件名提取日期
         * Hexo 标准格式：yyyy-MM-dd-title.md
         */
        private fun extractDateFromFileName(fileName: String): String {
            val pattern = """(\d{4}-\d{2}-\d{2})-.+\.md""".toRegex()
            val match = pattern.find(fileName)
            return match?.groupValues?.get(1) ?: getCurrentDate()
        }
        
        /**
         * 格式化日期为 Hexo 标准格式
         */
        private fun formatDate(date: Date): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(date)
        }
        
        /**
         * 获取当前日期字符串（Hexo 标准格式）
         */
        private fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
        
        /**
         * 创建新的 Post 对象
         */
        fun createNew(
            title: String, 
            bodyContent: String = "", 
            categories: List<String> = emptyList(), 
            tags: List<String> = emptyList(),
            additionalFrontMatter: Map<String, Any> = emptyMap()
        ): Post {
            val date = getCurrentDate()
            val fileName = generateFileName(date, title)
            
            // 创建标准的 Front-matter 数据
            val frontMatterData = YamlParser.createStandardFrontMatter(
                title = title,
                date = Date(),
                categories = categories,
                tags = tags,
                additionalData = additionalFrontMatter
            )
            
            // 生成完整的 Markdown 内容
            val fullContent = YamlParser.generateFrontMatter(frontMatterData) + bodyContent
            
            return Post(
                id = System.currentTimeMillis().toString(),
                title = title,
                fileName = fileName,
                content = fullContent,
                bodyContent = bodyContent,
                date = date,
                categories = categories,
                tags = tags,
                filePath = "", // 将在保存时设置
                lastModified = System.currentTimeMillis(),
                frontMatterData = frontMatterData
            )
        }
        
        /**
         * 生成文件名
         * 格式：YYYY-MM-DD-文章标题.md
         */
        private fun generateFileName(date: String, title: String): String {
            // 从日期字符串中提取日期部分（yyyy-MM-dd）
            val datePart = date.substring(0, 10)
            
            // 清理标题，生成安全的文件名
            val safeTitle = title
                .replace(" ", "-")
                .replace("[^a-zA-Z0-9\\-]".toRegex(), "")
                .lowercase()
                .take(50) // 限制长度
            
            return "$datePart-$safeTitle.md"
        }
        
        /**
         * 更新 Post 对象
         */
        fun updatePost(
            post: Post,
            title: String,
            bodyContent: String,
            categories: List<String>,
            tags: List<String>,
            additionalUpdates: Map<String, Any> = emptyMap()
        ): Post {
            // 合并 Front-matter 数据
            val updatedFrontMatter = mutableMapOf<String, Any>()
            
            // 保留原有的 Front-matter 数据
            updatedFrontMatter.putAll(post.frontMatterData)
            
            // 更新标准字段
            updatedFrontMatter["title"] = title
            
            // 更新日期（如果原来没有 date 字段，添加当前日期）
            if (!updatedFrontMatter.containsKey("date")) {
                updatedFrontMatter["date"] = getCurrentDate()
            }
            
            // 更新分类和标签
            if (categories.isNotEmpty()) {
                updatedFrontMatter["categories"] = categories
            } else if (updatedFrontMatter.containsKey("categories")) {
                updatedFrontMatter.remove("categories")
            }
            
            if (tags.isNotEmpty()) {
                updatedFrontMatter["tags"] = tags
            } else if (updatedFrontMatter.containsKey("tags")) {
                updatedFrontMatter.remove("tags")
            }
            
            // 应用额外更新
            updatedFrontMatter.putAll(additionalUpdates)
            
            // 生成新的文件名（如果需要）
            val newFileName = if (post.title != title) {
                generateFileName(post.date, title)
            } else {
                post.fileName
            }
            
            // 生成完整的 Markdown 内容
            val fullContent = YamlParser.generateFrontMatter(updatedFrontMatter) + bodyContent
            
            return post.copy(
                title = title,
                fileName = newFileName,
                content = fullContent,
                bodyContent = bodyContent,
                categories = categories,
                tags = tags,
                lastModified = System.currentTimeMillis(),
                frontMatterData = updatedFrontMatter
            )
        }
        
        /**
         * 生成完整的 Markdown 内容
         */
        fun generateMarkdownContent(post: Post): String {
            return YamlParser.generateFrontMatter(post.frontMatterData) + post.bodyContent
        }
    }
    
    /**
     * 转换为简化的 Blog 对象（用于列表显示）
     */
    fun toBlog(): com.example.hexobloguploader.Blog {
        return com.example.hexobloguploader.Blog(
            id = id,
            title = title,
            content = bodyContent.take(200), // 预览内容（只显示正文）
            date = date,
            tags = tags,
            filePath = filePath,
            lastModified = lastModified
        )
    }
}
