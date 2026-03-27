package com.example.hexobloguploader.storage

import android.content.Context
import com.example.hexobloguploader.model.Post
import java.io.File
import java.io.IOException

/**
 * Hexo 博客存储管理器
 * 负责管理博客文件的存储和读取
 */
class BlogStorageManager(private val context: Context) {
    
    companion object {
        const val BLOG_ROOT_DIR_NAME = "hexo-blog"
        const val POSTS_DIR_NAME = "source/_posts"
        const val IMAGES_DIR_NAME = "source/images"
    }
    
    /**
     * 获取博客根目录
     * 路径：/storage/emulated/0/Android/data/你的包名/files/hexo-blog/
     */
    val blogRootDir: File
        get() = File(context.getExternalFilesDir(null), BLOG_ROOT_DIR_NAME)
    
    /**
     * 获取博客文章目录
     * 路径：.../hexo-blog/source/_posts/
     */
    val postsDir: File
        get() = File(blogRootDir, POSTS_DIR_NAME)
    
    /**
     * 获取图片目录
     * 路径：.../hexo-blog/source/images/
     */
    val imagesDir: File
        get() = File(blogRootDir, IMAGES_DIR_NAME)
    
    /**
     * 初始化存储目录
     * 检查目录是否存在，如果不存在则创建
     */
    fun initStorage(): Boolean {
        return try {
            // 创建所有必要的目录
            val dirs = listOf(blogRootDir, postsDir, imagesDir)
            dirs.forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }
            }
            
            // 验证目录是否创建成功
            dirs.all { it.exists() && it.isDirectory }
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 检查存储目录是否已初始化
     */
    fun isStorageInitialized(): Boolean {
        return blogRootDir.exists() && 
               postsDir.exists() && 
               imagesDir.isDirectory
    }
    
    /**
     * 获取所有博客文章
     */
    fun getAllPosts(): List<Post> {
        if (!postsDir.exists()) {
            return emptyList()
        }
        
        return postsDir.listFiles { file ->
            file.isFile && file.name.endsWith(".md")
        }?.mapNotNull { file ->
            Post.fromFile(file)
        }?.sortedByDescending { post ->
            post.date
        } ?: emptyList()
    }
    
    /**
     * 根据ID获取博客文章
     */
    fun getPostById(id: String): Post? {
        return getAllPosts().find { it.id == id }
    }
    
    /**
     * 根据文件名获取博客文章
     */
    fun getPostByFileName(fileName: String): Post? {
        val file = File(postsDir, fileName)
        return if (file.exists()) Post.fromFile(file) else null
    }
    
        /**
         * 保存博客文章
         * @return 保存成功返回true，失败返回false
         */
        fun savePost(post: Post): Boolean {
            return try {
                // 确保目录存在
                if (!postsDir.exists()) {
                    postsDir.mkdirs()
                }
                
                // 确定文件名
                val fileName = if (post.fileName.isNotEmpty()) {
                    post.fileName
                } else {
                    // 从日期字符串中提取日期部分（yyyy-MM-dd）
                    val datePart = post.date.substring(0, 10)
                    val safeTitle = post.title
                        .replace(" ", "-")
                        .replace("[^a-zA-Z0-9\\-]".toRegex(), "")
                        .lowercase()
                        .take(50)
                    "$datePart-$safeTitle.md"
                }
                
                // 创建文件
                val file = File(postsDir, fileName)
                
                // 生成Markdown内容
                val content = Post.generateMarkdownContent(post)
                
                // 写入文件
                file.writeText(content)
                
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            } catch (e: SecurityException) {
                e.printStackTrace()
                false
            }
        }
    
    /**
     * 更新博客文章
     */
    fun updatePost(post: Post): Boolean {
        // 如果文件路径为空，视为新文章
        if (post.filePath.isEmpty()) {
            return savePost(post)
        }
        
        return try {
            val file = File(post.filePath)
            if (!file.exists()) {
                return savePost(post)
            }
            
            // 生成Markdown内容
            val content = Post.generateMarkdownContent(post)
            
            // 写入文件
            file.writeText(content)
            
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 删除博客文章
     */
    fun deletePost(post: Post): Boolean {
        return try {
            if (post.filePath.isEmpty()) {
                return false
            }
            
            val file = File(post.filePath)
            if (file.exists()) {
                file.delete()
            } else {
                false
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 删除博客文章（通过ID）
     */
    fun deletePostById(id: String): Boolean {
        val post = getPostById(id)
        return post != null && deletePost(post)
    }
    
    /**
     * 获取文章数量
     */
    fun getPostCount(): Int {
        if (!postsDir.exists()) {
            return 0
        }
        
        return postsDir.listFiles { file ->
            file.isFile && file.name.endsWith(".md")
        }?.size ?: 0
    }
    
    /**
     * 检查文章是否存在
     */
    fun postExists(fileName: String): Boolean {
        val file = File(postsDir, fileName)
        return file.exists() && file.isFile
    }
    
    /**
     * 获取存储使用情况
     */
    fun getStorageInfo(): StorageInfo {
        val totalSize = calculateDirectorySize(blogRootDir)
        val postCount = getPostCount()
        
        return StorageInfo(
            totalSize = totalSize,
            postCount = postCount,
            blogRootPath = blogRootDir.absolutePath,
            postsPath = postsDir.absolutePath,
            imagesPath = imagesDir.absolutePath
        )
    }
    
    /**
     * 计算目录大小
     */
    private fun calculateDirectorySize(directory: File): Long {
        if (!directory.exists() || !directory.isDirectory) {
            return 0
        }
        
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isFile) {
                file.length()
            } else {
                calculateDirectorySize(file)
            }
        }
        
        return size
    }
    
    /**
     * 清理临时文件
     */
    fun cleanupTempFiles(): Boolean {
        return try {
            // 这里可以添加清理临时文件的逻辑
            // 例如：删除 .tmp 文件、清理缓存等
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 导出博客文章到指定目录
     */
    fun exportPosts(targetDir: File): Boolean {
        return try {
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            
            val posts = getAllPosts()
            posts.forEach { post ->
                val targetFile = File(targetDir, post.fileName)
                val content = Post.generateMarkdownContent(post)
                targetFile.writeText(content)
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}

/**
 * 存储信息数据类
 */
data class StorageInfo(
    val totalSize: Long,          // 总大小（字节）
    val postCount: Int,           // 文章数量
    val blogRootPath: String,     // 博客根目录路径
    val postsPath: String,        // 文章目录路径
    val imagesPath: String        // 图片目录路径
) {
    /**
     * 获取格式化的大小字符串
     */
    fun getFormattedSize(): String {
        return when {
            totalSize < 1024 -> "$totalSize B"
            totalSize < 1024 * 1024 -> "${totalSize / 1024} KB"
            totalSize < 1024 * 1024 * 1024 -> "${totalSize / (1024 * 1024)} MB"
            else -> "${totalSize / (1024 * 1024 * 1024)} GB"
        }
    }
}