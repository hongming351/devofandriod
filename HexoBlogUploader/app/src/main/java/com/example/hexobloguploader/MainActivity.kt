package com.example.hexobloguploader

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.hexobloguploader.databinding.ActivityMainBinding
import com.example.hexobloguploader.storage.BlogStorageManager

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var storageManager: BlogStorageManager
    
    companion object {
        private const val REQUEST_EDIT_POST = 1001
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "MainActivity onCreate started")
            
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            setSupportActionBar(binding.toolbar)
            supportActionBar?.title = "Hexo 博客上传器"

            storageManager = BlogStorageManager(this)

            setupRecyclerView()
            setupFab()
            setupMenu()
            loadBlogs()
            
            Log.d(TAG, "MainActivity onCreate completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error in MainActivity onCreate: ${e.message}", e)
            showToast("应用启动失败: ${e.message}")
            // 显示错误界面
            updateEmptyState(true)
        }
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Hexo 博客管理"
    }
    
    private fun setupRecyclerView() {
        blogAdapter = BlogAdapter { blog ->
            // 点击博客项的处理
            showBlogDetail(blog)
        }
        
        binding.recyclerViewBlogs.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = blogAdapter
            setHasFixedSize(true)
        }
    }
    
    private fun setupFab() {
        binding.fabAddBlog.setOnClickListener {
            // 打开 Markdown 编辑器创建新文章
            val intent = android.content.Intent(this, EditPostActivity::class.java)
            startActivityForResult(intent, REQUEST_EDIT_POST)
        }
    }
    
    private fun setupMenu() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_git_manager -> {
                    // 打开 Git 管理界面
                    val intent = android.content.Intent(this, SimpleGitManagerActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.menu_refresh -> {
                    // 刷新文章列表
                    loadBlogs()
                    true
                }
                R.id.menu_settings -> {
                    // 打开设置界面（待实现）
                    showToast("设置功能待实现")
                    true
                }
                else -> false
            }
        }
    }
    
    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    private fun loadBlogs() {
        // 初始化存储
        if (!storageManager.initStorage()) {
            showToast("存储初始化失败，请检查权限")
            return
        }
        
        // 加载博客文章
        loadBlogsFromStorage()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_EDIT_POST && resultCode == RESULT_OK) {
            // 文章编辑/创建成功，刷新列表
            loadBlogs()
            showToast("文章保存成功")
        }
    }
    
    private fun initStorage() {
        val success = storageManager.initStorage()
        if (success) {
            showToast("存储初始化成功")
            
            // 显示存储信息
            val storageInfo = storageManager.getStorageInfo()
            binding.toolbar.subtitle = "文章: ${storageInfo.postCount} | 大小: ${storageInfo.getFormattedSize()}"
        } else {
            showToast("存储初始化失败，请检查权限")
        }
    }
    
    private fun loadBlogsFromStorage() {
        val posts = storageManager.getAllPosts()
        
        if (posts.isNotEmpty()) {
            // 从存储加载真实数据
            val blogs = posts.map { it.toBlog() }
            blogAdapter.submitList(blogs)
            updateEmptyState(false)
            
            // 更新存储信息
            val storageInfo = storageManager.getStorageInfo()
            binding.toolbar.subtitle = "文章: ${storageInfo.postCount} | 大小: ${storageInfo.getFormattedSize()}"
        } else {
            // 如果没有文章，加载示例数据
            loadSampleBlogs()
        }
    }
    
    private fun loadSampleBlogs() {
        val sampleBlogs = listOf(
            Blog(
                id = "1",
                title = "欢迎使用 Hexo 博客上传器",
                content = "这是你的第一篇博客文章，点击编辑开始写作吧！",
                date = "2025-03-25",
                tags = listOf("欢迎", "指南"),
                filePath = "/sample/blog1.md"
            ),
            Blog(
                id = "2", 
                title = "Android 开发入门",
                content = "学习如何使用 Kotlin 开发 Android 应用",
                date = "2025-03-24",
                tags = listOf("Android", "Kotlin", "教程"),
                filePath = "/sample/blog2.md"
            ),
            Blog(
                id = "3",
                title = "Markdown 写作技巧",
                content = "掌握 Markdown 语法，提升写作效率",
                date = "2025-03-23",
                tags = listOf("Markdown", "写作", "技巧"),
                filePath = "/sample/blog3.md"
            )
        )
        
        blogAdapter.submitList(sampleBlogs)
        updateEmptyState(false)
        
        // 显示存储路径信息
        val storageInfo = storageManager.getStorageInfo()
        binding.toolbar.subtitle = "存储路径: ${storageInfo.blogRootPath}"
    }
    
    private fun showBlogDetail(blog: Blog) {
        val intent = android.content.Intent(this, BlogDetailActivity::class.java).apply {
            // 使用Bundle传递数据
            val bundle = Bundle().apply {
                putString("id", blog.id)
                putString("title", blog.title)
                putString("content", blog.content)
                putString("date", blog.date)
                putStringArrayList("tags", ArrayList(blog.tags))
                putString("filePath", blog.filePath)
            }
            putExtra("BLOG_DATA", bundle)
        }
        startActivity(intent)
    }
    
    private fun createNewBlog() {
        // 后续将实现创建新博客页面
        showToast("创建新博客")
        
        // 暂时创建一个示例博客并保存
        val newPost = com.example.hexobloguploader.model.Post.createNew(
            title = "新博客文章",
            content = "# 新博客文章\n\n这是你的新博客文章，开始编辑吧！",
            categories = listOf("未分类"),
            tags = listOf("新文章")
        )
        
        val success = storageManager.savePost(newPost)
        if (success) {
            showToast("博客创建成功")
            loadBlogsFromStorage() // 重新加载列表
        } else {
            showToast("博客创建失败")
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.textEmptyState.visibility = if (isEmpty) android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerViewBlogs.visibility = if (isEmpty) android.view.View.GONE else android.view.View.VISIBLE
    }
    
    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}

// 博客数据类（简化版，用于列表显示）
data class Blog(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val tags: List<String>,
    val filePath: String
)
