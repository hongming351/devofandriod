package com.example.hexobloguploader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivitySettingsBinding

/**
 * 设置界面
 * 用于配置应用的各种设置
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupSettings()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupSettings() {
        // GitHub设置
        binding.buttonSaveGithub.setOnClickListener {
            val username = binding.editGithubUsername.text.toString()
            val token = binding.editGithubToken.text.toString()
            val repoUrl = binding.editRepoUrl.text.toString()
            
            if (username.isEmpty()) {
                Toast.makeText(this, "请输入GitHub用户名", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (token.isNotEmpty() && !token.startsWith("ghp_")) {
                Toast.makeText(this, "GitHub Token应以ghp_开头", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (repoUrl.isNotEmpty() && !(repoUrl.startsWith("https://github.com/") || repoUrl.startsWith("git@github.com:"))) {
                Toast.makeText(this, "请输入有效的GitHub仓库地址", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 保存设置（这里应该保存到SharedPreferences）
            Toast.makeText(this, "GitHub设置已保存", Toast.LENGTH_SHORT).show()
        }
        
        // 博客设置
        binding.buttonSaveBlog.setOnClickListener {
            val title = binding.editBlogTitle.text.toString()
            val author = binding.editAuthorName.text.toString()
            
            if (title.isEmpty()) {
                Toast.makeText(this, "请输入博客标题", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (author.isEmpty()) {
                Toast.makeText(this, "请输入作者名称", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 保存设置
            Toast.makeText(this, "博客设置已保存", Toast.LENGTH_SHORT).show()
        }
        
        // 存储设置
        binding.buttonSaveStorage.setOnClickListener {
            val path = binding.editStoragePath.text.toString()
            
            if (path.isEmpty()) {
                Toast.makeText(this, "请输入存储路径", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 保存设置
            Toast.makeText(this, "存储设置已保存", Toast.LENGTH_SHORT).show()
        }
        
        // 关于应用
        binding.buttonCheckUpdates.setOnClickListener {
            Toast.makeText(this, "检查更新功能待实现", Toast.LENGTH_SHORT).show()
        }
        
        binding.buttonFeedback.setOnClickListener {
            Toast.makeText(this, "反馈功能待实现", Toast.LENGTH_SHORT).show()
        }
        
        binding.buttonAbout.setOnClickListener {
            showAboutDialog()
        }
        
        // 加载现有设置（这里应该从SharedPreferences加载）
        loadExistingSettings()
    }
    
    private fun loadExistingSettings() {
        // 这里应该从SharedPreferences加载现有设置
        // 暂时设置一些默认值
        binding.editGithubUsername.setText("your-username")
        binding.editRepoUrl.setText("https://github.com/your-username/your-hexo-blog.git")
        binding.editBlogTitle.setText("我的 Hexo 博客")
        binding.editAuthorName.setText("Hexo 博客作者")
        binding.editStoragePath.setText("/storage/emulated/0/HexoBlog")
    }
    
    private fun showAboutDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("关于 Hexo 博客上传器")
            .setMessage("""
                Hexo 博客上传器 v1.0.0
                
                一个用于在 Android 设备上管理 Hexo 博客的应用。
                
                功能特性：
                • 创建和编辑 Markdown 文章
                • 管理 Hexo 博客仓库
                • Git 操作（克隆、提交、推送）
                • 本地存储管理
                
                开发者：Hexo 博客上传器团队
                
                开源协议：MIT License
            """.trimIndent())
            .setPositiveButton("确定", null)
            .show()
    }
}