package com.example.hexobloguploader.utils

import android.util.Log
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.error.YAMLException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * YAML 解析工具类
 * 用于解析 Hexo 博客的 Front-matter
 */
object YamlParser {
    
    private const val TAG = "YamlParser"
    private val yaml = Yaml()
    
    /**
     * 解析 Front-matter 内容
     * @param content 完整的 Markdown 文件内容
     * @return Pair<FrontMatter, Body>，其中 FrontMatter 是解析后的 YAML 数据，Body 是正文内容
     */
    fun parseFrontMatter(content: String): Pair<Map<String, Any>?, String> {
        return try {
            // 检查是否以 --- 开头
            if (!content.trimStart().startsWith("---")) {
                // 没有 Front-matter，整个内容都是正文
                return Pair(null, content)
            }
            
            val lines = content.lines()
            var frontMatterStart = -1
            var frontMatterEnd = -1
            
            // 查找第一个 ---
            for (i in lines.indices) {
                if (lines[i].trim() == "---") {
                    frontMatterStart = i
                    break
                }
            }
            
            if (frontMatterStart == -1) {
                // 没有找到开始的 ---
                return Pair(null, content)
            }
            
            // 查找第二个 ---
            for (i in frontMatterStart + 1 until lines.size) {
                if (lines[i].trim() == "---") {
                    frontMatterEnd = i
                    break
                }
            }
            
            if (frontMatterEnd == -1) {
                // 没有找到结束的 ---
                return Pair(null, content)
            }
            
            // 提取 Front-matter 内容
            val frontMatterContent = lines.subList(frontMatterStart + 1, frontMatterEnd)
                .joinToString("\n")
            
            // 解析 YAML
            val parsedData = yaml.load<Map<String, Any>>(frontMatterContent)
            
            // 提取正文内容
            val bodyContent = lines.subList(frontMatterEnd + 1, lines.size)
                .joinToString("\n")
            
            Pair(parsedData, bodyContent)
            
        } catch (e: YAMLException) {
            Log.e(TAG, "YAML 解析失败: ${e.message}")
            // 解析失败，返回空 Front-matter 和完整内容
            Pair(null, content)
        } catch (e: Exception) {
            Log.e(TAG, "解析 Front-matter 失败: ${e.message}")
            Pair(null, content)
        }
    }
    
    /**
     * 从 Front-matter 中获取字符串值
     */
    fun getStringValue(data: Map<String, Any>?, key: String, defaultValue: String = ""): String {
        return try {
            val value = data?.get(key)
            when (value) {
                is String -> value
                is List<*> -> value.joinToString(", ")
                else -> value?.toString() ?: defaultValue
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取字符串值失败: ${e.message}")
            defaultValue
        }
    }
    
    /**
     * 从 Front-matter 中获取字符串列表
     */
    fun getStringList(data: Map<String, Any>?, key: String): List<String> {
        return try {
            val value = data?.get(key)
            when (value) {
                is List<*> -> value.filterIsInstance<String>()
                is String -> {
                    // 处理字符串格式的列表，如 "[tag1, tag2]" 或 "tag1, tag2"
                    parseStringToList(value)
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取字符串列表失败: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 解析字符串为列表
     * 支持格式: "[tag1, tag2]" 或 "tag1, tag2"
     */
    private fun parseStringToList(value: String): List<String> {
        return try {
            val trimmed = value.trim()
            if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
                // 移除方括号并分割
                trimmed.substring(1, trimmed.length - 1)
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            } else {
                // 直接按逗号分割
                trimmed.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 从 Front-matter 中获取日期值
     */
    fun getDateValue(data: Map<String, Any>?, key: String): Date? {
        return try {
            val value = data?.get(key)
            when (value) {
                is Date -> value
                is String -> {
                    // 尝试解析常见日期格式
                    parseDateString(value)
                }
                else -> null
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取日期值失败: ${e.message}")
            null
        }
    }
    
    /**
     * 解析日期字符串
     * 支持格式: "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd", ISO 8601 等
     */
    private fun parseDateString(dateStr: String): Date? {
        return try {
            val formats = listOf(
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd",
                "yyyy/MM/dd HH:mm:ss",
                "yyyy/MM/dd",
                "yyyy.MM.dd HH:mm:ss",
                "yyyy.MM.dd"
            )
            
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    sdf.isLenient = false
                    return sdf.parse(dateStr)
                } catch (e: Exception) {
                    // 尝试下一个格式
                    continue
                }
            }
            
            null
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 生成 Front-matter 字符串
     * @param data Front-matter 数据
     * @return 格式化的 Front-matter 字符串（包含 --- 分隔符）
     */
    fun generateFrontMatter(data: Map<String, Any>): String {
        return try {
            val yamlContent = yaml.dump(data)
            "---\n$yamlContent---\n"
        } catch (e: Exception) {
            Log.e(TAG, "生成 Front-matter 失败: ${e.message}")
            "---\n---\n"
        }
    }
    
    /**
     * 合并 Front-matter 数据
     * @param original 原始 Front-matter 数据
     * @param updates 要更新的数据
     * @return 合并后的 Front-matter 数据
     */
    fun mergeFrontMatter(original: Map<String, Any>?, updates: Map<String, Any>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        // 添加原始数据（如果存在）
        original?.forEach { (key, value) ->
            result[key] = value
        }
        
        // 更新数据（覆盖相同键的值）
        updates.forEach { (key, value) ->
            result[key] = value
        }
        
        return result
    }
    
    /**
     * 创建标准的 Hexo Front-matter 数据
     */
    fun createStandardFrontMatter(
        title: String,
        date: Date = Date(),
        categories: List<String> = emptyList(),
        tags: List<String> = emptyList(),
        additionalData: Map<String, Any> = emptyMap()
    ): Map<String, Any> {
        val data = mutableMapOf<String, Any>()
        
        // 标准字段
        data["title"] = title
        
        // 格式化日期为 Hexo 标准格式
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        data["date"] = dateFormat.format(date)
        
        // 分类和标签
        if (categories.isNotEmpty()) {
            data["categories"] = categories
        }
        
        if (tags.isNotEmpty()) {
            data["tags"] = tags
        }
        
        // 添加额外数据
        data.putAll(additionalData)
        
        return data
    }
    
    /**
     * 提取文章标题
     * 优先从 Front-matter 获取，如果没有则从正文第一行获取
     */
    fun extractTitle(content: String): String {
        return try {
            val (frontMatter, body) = parseFrontMatter(content)
            
            // 优先从 Front-matter 获取标题
            val frontMatterTitle = getStringValue(frontMatter, "title")
            if (frontMatterTitle.isNotEmpty()) {
                return frontMatterTitle
            }
            
            // 从正文第一行获取标题（Markdown 的 # 标题）
            val firstLine = body.lines().firstOrNull { it.isNotEmpty() }
            if (firstLine != null && firstLine.startsWith("# ")) {
                return firstLine.substring(2).trim()
            }
            
            // 默认标题
            "未命名文章"
        } catch (e: Exception) {
            Log.e(TAG, "提取标题失败: ${e.message}")
            "未命名文章"
        }
    }
}