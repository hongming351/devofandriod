package com.example.hexobloguploader

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityBlogDetailBinding

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
    }
    
    private fun loadBlogData() {
        // 从 Intent 获取博客数据
        val blog = intent.getSerializableExtra("BLOG_DATA") as? Blog
        
        if (blog != null) {
            supportActionBar?.title = blog.title
            binding.textBlogTitle.text = blog.title
            binding.textBlogDate.text = "发布日期: ${blog.date}"
            binding.textBlogContent.text = blog.content
            
            // 显示标签
            binding.textBlogTags.text = "标签: ${blog.tags.joinToString(", ")}"
            binding.textFilePath.text = "文件路径: ${blog.filePath}"
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
        // 后续将实现编辑功能
        android.widget.Toast.makeText(
            this,
            "打开编辑器",
            android.widget.Toast.LENGTH_SHORT
        ).show()
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
}