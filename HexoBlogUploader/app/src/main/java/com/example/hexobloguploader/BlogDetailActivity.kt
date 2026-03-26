package com.example.hexobloguploader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.example.hexobloguploader.databinding.ActivityBlogDetailBinding
import java.io.File

class BlogDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBlogDetailBinding
    
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
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
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
        // 后续将实现预览功能
        android.widget.Toast.makeText(
            this,
            "打开预览",
            android.widget.Toast.LENGTH_SHORT
        ).show()
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