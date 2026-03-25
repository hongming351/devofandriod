package com.example.hexobloguploader

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityEditPostBinding
import com.example.hexobloguploader.model.Post
import com.example.hexobloguploader.storage.BlogStorageManager
import io.noties.markwon.Markwon
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Markdown 编辑器 Activity
 * 用于创建和编辑 Hexo 博客文章
 */
class EditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditPostBinding
    private lateinit var storageManager: BlogStorageManager
    private lateinit var markwon: Markwon
    
    private var currentPost: Post? = null
    private var isNewPost: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        storageManager = BlogStorageManager(this)
        markwon = Markwon.create(this)
        
        setupToolbar()
        loadPostData()
        setupEditTextListener()
        setupButtons()
        updatePreview()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
    
    private fun loadPostData() {
        // 获取传递的 Post 数据
        val post = intent.getSerializableExtra("POST_DATA") as? Post
        
        if (post != null) {
            // 编辑现有文章
            currentPost = post
            isNewPost = false
            
            supportActionBar?.title = "编辑文章"
            binding.editTextTitle.setText(post.title)
            binding.editTextContent.setText(post.content)
            
            // 显示标签和分类
            binding.editTextTags.setText(post.tags.joinToString(", "))
            binding.editTextCategories.setText(post.categories.joinToString(", "))
            
        } else {
            // 创建新文章
            isNewPost = true
            supportActionBar?.title = "新建文章"
            
            // 设置默认标题
            binding.editTextTitle.setText("新博客文章")
            
            // 生成默认内容
            val defaultContent = generateDefaultContent()
            binding.editTextContent.setText(defaultContent)
        }
    }
    
    private fun generateDefaultContent(): String {
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        
        return """---
title: 新博客文章
date: $date
tags: []
categories: []
---

# 新博客文章

开始写作吧！

## 功能说明

1. 支持 Markdown 语法
2. 实时预览
3. 自动保存
4. Hexo Front-matter 支持

## 写作提示

- 使用 # 表示标题
- 使用 * 或 - 表示列表
- 使用 **粗体** 和 *斜体*
- 使用 ``` 表示代码块

**祝你写作愉快！**
"""
    }
    
    private fun setupEditTextListener() {
        // 内容变化时更新预览
        binding.editTextContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updatePreview()
            }
        })
        
        // 标题变化时更新工具栏
        binding.editTextTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val title = s?.toString()?.trim()
                if (!title.isNullOrEmpty()) {
                    supportActionBar?.title = title
                }
            }
        })
    }
    
    private fun setupButtons() {
        binding.buttonSave.setOnClickListener {
            savePost()
        }
        
        binding.buttonPreview.setOnClickListener {
            togglePreviewMode()
        }
        
        binding.buttonDelete.setOnClickListener {
            deletePost()
        }
        
        // 新建文章时不显示删除按钮
        binding.buttonDelete.visibility = if (isNewPost) android.view.View.GONE else android.view.View.VISIBLE
    }
    
    private fun updatePreview() {
        val content = binding.editTextContent.text.toString()
        
        // 使用 Markwon 渲染 Markdown
        markwon.setMarkdown(binding.textPreview, content)
        
        // 更新字符统计
        val charCount = content.length
        val wordCount = content.split("\\s+".toRegex()).count { it.isNotEmpty() }
        binding.textStats.text = "字符: $charCount | 单词: $wordCount"
    }
    
    private fun togglePreviewMode() {
        if (binding.scrollViewPreview.visibility == android.view.View.VISIBLE) {
            // 切换到编辑模式
            binding.scrollViewPreview.visibility = android.view.View.GONE
            binding.scrollViewEdit.visibility = android.view.View.VISIBLE
            binding.buttonPreview.text = "预览"
        } else {
            // 切换到预览模式
            binding.scrollViewEdit.visibility = android.view.View.GONE
            binding.scrollViewPreview.visibility = android.view.View.VISIBLE
            binding.buttonPreview.text = "编辑"
            updatePreview()
        }
    }
    
    private fun savePost() {
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString().trim()
        val tagsText = binding.editTextTags.text.toString().trim()
        val categoriesText = binding.editTextCategories.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(this, "请输入文章标题", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (content.isEmpty()) {
            Toast.makeText(this, "请输入文章内容", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 解析标签和分类
        val tags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        val categories = categoriesText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        
        val post = if (isNewPost) {
            // 创建新文章
            Post.createNew(
                title = title,
                content = content,
                categories = categories,
                tags = tags
            )
        } else {
            // 更新现有文章
            currentPost?.copy(
                title = title,
                content = content,
                categories = categories,
                tags = tags,
                lastModified = System.currentTimeMillis()
            )
        }
        
        if (post != null) {
            val success = storageManager.savePost(post)
            
            if (success) {
                Toast.makeText(this, "文章保存成功", Toast.LENGTH_SHORT).show()
                
                // 检查是否自动提交到 Git
                if (shouldAutoCommit()) {
                    autoCommitToGit(post.title)
                }
                
                // 返回结果
                setResult(RESULT_OK)
                finish()
            } else {
                Toast.makeText(this, "文章保存失败", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    /**
     * 自动提交到 Git
     */
    private fun autoCommitToGit(postTitle: String) {
        // 这里应该从设置中获取 Token
        val gitToken = getGitToken()
        
        if (gitToken.isNotEmpty() && gitToken.startsWith("ghp_")) {
            // 在后台执行 Git 提交
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val gitManager = com.example.hexobloguploader.git.GitOperationsManager(this@EditPostActivity)
                
                // 生成提交信息
                val commitMessage = if (isNewPost) {
                    "新增文章: $postTitle"
                } else {
                    "更新文章: $postTitle"
                }
                
                val result = gitManager.commitAndPush(commitMessage, gitToken, true)
                
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    if (result.success) {
                        Toast.makeText(this@EditPostActivity, "文章已自动提交到 Git", Toast.LENGTH_SHORT).show()
                    } else {
                        // 提交失败，但不影响文章保存
                        Log.w("EditPostActivity", "Git 提交失败: ${result.message}")
                    }
                }
            }
        }
    }
    
    /**
     * 检查是否应该自动提交
     */
    private fun shouldAutoCommit(): Boolean {
        // 这里应该从设置中读取用户偏好
        // 暂时返回 true 进行测试
        return true
    }
    
    /**
     * 获取 Git Token
     */
    private fun getGitToken(): String {
        // 这里应该从安全存储中获取 Token
        // 暂时返回空字符串
        return ""
    }
    
    private fun deletePost() {
        if (currentPost != null && !isNewPost) {
            // 显示确认对话框
            android.app.AlertDialog.Builder(this)
                .setTitle("删除文章")
                .setMessage("确定要删除这篇文章吗？此操作不可撤销。")
                .setPositiveButton("删除") { _, _ ->
                    val success = storageManager.deletePost(currentPost!!)
                    
                    if (success) {
                        Toast.makeText(this, "文章删除成功", Toast.LENGTH_SHORT).show()
                        
                        // 返回结果
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        Toast.makeText(this, "文章删除失败", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
    
    override fun onBackPressed() {
        // 检查是否有未保存的更改
        val title = binding.editTextTitle.text.toString().trim()
        val content = binding.editTextContent.text.toString().trim()
        
        val hasChanges = if (isNewPost) {
            title.isNotEmpty() || content.isNotEmpty()
        } else {
            val originalTitle = currentPost?.title ?: ""
            val originalContent = currentPost?.content ?: ""
            title != originalTitle || content != originalContent
        }
        
        if (hasChanges) {
            android.app.AlertDialog.Builder(this)
                .setTitle("未保存的更改")
                .setMessage("您有未保存的更改，是否要保存？")
                .setPositiveButton("保存") { _, _ ->
                    savePost()
                }
                .setNegativeButton("不保存") { _, _ ->
                    super.onBackPressed()
                }
                .setNeutralButton("取消", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}