package com.example.hexobloguploader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityTestStorageBinding
import com.example.hexobloguploader.model.Post
import com.example.hexobloguploader.storage.BlogStorageManager

/**
 * 测试存储功能的Activity
 * 用于验证数据模型和存储结构是否正常工作
 */
class TestStorageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestStorageBinding
    private lateinit var storageManager: BlogStorageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestStorageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        storageManager = BlogStorageManager(this)
        
        setupButtons()
        updateStorageInfo()
    }
    
    private fun setupButtons() {
        binding.buttonInitStorage.setOnClickListener {
            initStorage()
        }
        
        binding.buttonCreateTestPost.setOnClickListener {
            createTestPost()
        }
        
        binding.buttonListPosts.setOnClickListener {
            listPosts()
        }
        
        binding.buttonClearTestData.setOnClickListener {
            clearTestData()
        }
        
        binding.buttonBack.setOnClickListener {
            finish()
        }
    }
    
    private fun initStorage() {
        val success = storageManager.initStorage()
        if (success) {
            appendLog("✅ 存储初始化成功")
            updateStorageInfo()
        } else {
            appendLog("❌ 存储初始化失败")
        }
    }
    
    private fun createTestPost() {
        val testPost = Post.createNew(
            title = "测试博客文章 ${System.currentTimeMillis() % 1000}",
            content = """
                # 测试博客文章
                
                这是一篇测试用的博客文章，用于验证存储功能。
                
                ## 功能测试
                
                1. 文章创建
                2. 文件保存
                3. 内容读取
                4. 列表显示
                
                ## 代码示例
                
                ```kotlin
                fun main() {
                    println("Hello, Hexo Blog Uploader!")
                }
                ```
                
                **测试完成！**
            """.trimIndent(),
            categories = listOf("测试", "开发"),
            tags = listOf("Android", "Kotlin", "Hexo")
        )
        
        val success = storageManager.savePost(testPost)
        if (success) {
            appendLog("✅ 测试文章创建成功: ${testPost.title}")
            updateStorageInfo()
        } else {
            appendLog("❌ 测试文章创建失败")
        }
    }
    
    private fun listPosts() {
        val posts = storageManager.getAllPosts()
        
        appendLog("📋 文章列表 (共 ${posts.size} 篇):")
        
        if (posts.isEmpty()) {
            appendLog("   暂无文章")
        } else {
            posts.forEachIndexed { index, post ->
                appendLog("   ${index + 1}. ${post.title} (${post.date})")
                appendLog("      文件: ${post.fileName}")
                appendLog("      分类: ${post.categories.joinToString(", ")}")
                appendLog("      标签: ${post.tags.joinToString(", ")}")
            }
        }
    }
    
    private fun clearTestData() {
        val posts = storageManager.getAllPosts()
        var deletedCount = 0
        
        posts.forEach { post ->
            if (post.title.contains("测试博客文章")) {
                if (storageManager.deletePost(post)) {
                    deletedCount++
                }
            }
        }
        
        appendLog("🗑️ 已删除 $deletedCount 篇测试文章")
        updateStorageInfo()
    }
    
    private fun updateStorageInfo() {
        val storageInfo = storageManager.getStorageInfo()
        
        binding.textStorageInfo.text = """
            📁 存储信息:
            
            根目录: ${storageInfo.blogRootPath}
            文章目录: ${storageInfo.postsPath}
            图片目录: ${storageInfo.imagesPath}
            
            文章数量: ${storageInfo.postCount}
            存储大小: ${storageInfo.getFormattedSize()}
            
            状态: ${if (storageManager.isStorageInitialized()) "✅ 已初始化" else "❌ 未初始化"}
        """.trimIndent()
    }
    
    private fun appendLog(message: String) {
        val currentText = binding.textLog.text.toString()
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        binding.textLog.text = "$currentText\n[$timestamp] $message"
        
        // 滚动到底部
        binding.scrollView.post {
            binding.scrollView.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }
}