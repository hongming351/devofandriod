package com.example.hexobloguploader.model

import java.io.File
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
    val content: String,           // Markdown 内容
    val date: String,              // 发布日期，格式：yyyy-MM-dd
    val categories: List<String>,  // 分类
    val tags: List<String>,        // 标签
    val filePath: String,          // 完整文件路径
    val lastModified: Long = System.currentTimeMillis()
) {
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
            val date = extractDateFromFileName(fileName)
            
            // 从内容提取标题和元数据
            val (title, categories, tags) = parseFrontMatter(content)
            
            return Post(
                id = id,
                title = title,
                fileName = fileName,
                content = content,
                date = date,
                categories = categories,
                tags = tags,
                filePath = file.absolutePath,
                lastModified = file.lastModified()
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
         * 解析 Front Matter（YAML 格式）
         * ---
         * title: 文章标题
         * date: 2025-03-25
         * categories: [分类1, 分类2]
         * tags: [标签1, 标签2]
         * ---
         */
        private fun parseFrontMatter(content: String): Triple<String, List<String>, List<String>> {
            var title = "未命名文章"
            var categories = emptyList<String>()
            var tags = emptyList<String>()
            
            val frontMatterRegex = """^---\s*\n(.*?)\n---\s*\n""".toRegex(RegexOption.DOT_MATCHES_ALL)
            val match = frontMatterRegex.find(content)
            
            if (match != null) {
                val frontMatter = match.groupValues[1]
                
                // 解析标题
                val titleRegex = """title:\s*(.+)""".toRegex()
                val titleMatch = titleRegex.find(frontMatter)
                title = titleMatch?.groupValues?.get(1)?.trim() ?: title
                
                // 解析分类
                val categoriesRegex = """categories:\s*\[(.*?)\]""".toRegex()
                val categoriesMatch = categoriesRegex.find(frontMatter)
                categories = categoriesMatch?.groupValues?.get(1)
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
                
                // 解析标签
                val tagsRegex = """tags:\s*\[(.*?)\]""".toRegex()
                val tagsMatch = tagsRegex.find(frontMatter)
                tags = tagsMatch?.groupValues?.get(1)
                    ?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotEmpty() }
                    ?: emptyList()
            } else {
                // 如果没有 Front Matter，使用第一行作为标题
                val firstLine = content.lines().firstOrNull { it.isNotEmpty() }
                if (firstLine != null && firstLine.startsWith("# ")) {
                    title = firstLine.substring(2).trim()
                }
            }
            
            return Triple(title, categories, tags)
        }
        
        /**
         * 获取当前日期字符串
         */
        private fun getCurrentDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            return sdf.format(Date())
        }
        
        /**
         * 创建新的 Post 对象
         */
        fun createNew(title: String, content: String = "", categories: List<String> = emptyList(), tags: List<String> = emptyList()): Post {
            val date = getCurrentDate()
            val fileName = "${date}-${title.replace(" ", "-").replace("[^a-zA-Z0-9-]".toRegex(), "").lowercase()}.md"
            
            return Post(
                id = System.currentTimeMillis().toString(),
                title = title,
                fileName = fileName,
                content = content,
                date = date,
                categories = categories,
                tags = tags,
                filePath = "", // 将在保存时设置
                lastModified = System.currentTimeMillis()
            )
        }
        
        /**
         * 生成 Hexo 格式的 Front Matter
         */
        fun generateFrontMatter(post: Post): String {
            return """---
title: ${post.title}
date: ${post.date}
categories: [${post.categories.joinToString(", ")}]
tags: [${post.tags.joinToString(", ")}]
---

"""
        }
        
        /**
         * 生成完整的 Markdown 内容
         */
        fun generateMarkdownContent(post: Post): String {
            val frontMatter = generateFrontMatter(post)
            return frontMatter + post.content
        }
    }
    
    /**
     * 转换为简化的 Blog 对象（用于列表显示）
     */
    fun toBlog(): com.example.hexobloguploader.Blog {
        return com.example.hexobloguploader.Blog(
            id = id,
            title = title,
            content = content.take(200), // 预览内容
            date = date,
            tags = tags,
            filePath = filePath
        )
    }
}