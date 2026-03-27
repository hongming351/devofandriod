package com.example.hexobloguploader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivitySettingsBinding
import com.example.hexobloguploader.git.GitSettingsManager

/**
 * 设置界面
 * 用于配置应用的各种设置
 */
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val sharedPreferences by lazy {
        getSharedPreferences("hexo_blog_settings", android.content.Context.MODE_PRIVATE)
    }
    
    companion object {
        private const val KEY_GITHUB_USERNAME = "github_username"
        private const val KEY_GITHUB_TOKEN = "github_token"
        private const val KEY_REPO_URL = "repo_url"
        private const val KEY_BLOG_TITLE = "blog_title"
        private const val KEY_AUTHOR_NAME = "author_name"
        private const val KEY_STORAGE_PATH = "storage_path"
    }
    
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
            
            // 使用 GitSettingsManager 保存设置
            val gitSettingsManager = GitSettingsManager(this)
            gitSettingsManager.setGitHubUsername(username)
            gitSettingsManager.setGitHubToken(token)
            gitSettingsManager.setRepoUrl(repoUrl)
            
            // 同时保存到旧的 SharedPreferences（兼容性）
            with(sharedPreferences.edit()) {
                putString(KEY_GITHUB_USERNAME, username)
                putString(KEY_GITHUB_TOKEN, token)
                putString(KEY_REPO_URL, repoUrl)
                apply()
            }
            
            // 显示 Git 设置状态
            val status = gitSettingsManager.getGitSetupStatus()
            if (status.isComplete) {
                Toast.makeText(this, "GitHub设置已保存，设置完整", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "GitHub设置已保存，但设置不完整: ${status.missingFields.joinToString()}", Toast.LENGTH_LONG).show()
            }
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
            with(sharedPreferences.edit()) {
                putString(KEY_BLOG_TITLE, title)
                putString(KEY_AUTHOR_NAME, author)
                apply()
            }
            
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
            with(sharedPreferences.edit()) {
                putString(KEY_STORAGE_PATH, path)
                apply()
            }
            
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
        
        // 加载现有设置
        loadExistingSettings()
    }
    
    private fun loadExistingSettings() {
        // 使用 GitSettingsManager 加载 Git 设置
        val gitSettingsManager = GitSettingsManager(this)
        
        // 优先从 GitSettingsManager 加载，如果为空则从旧的 SharedPreferences 加载
        val username = gitSettingsManager.getGitHubUsername().ifEmpty {
            sharedPreferences.getString(KEY_GITHUB_USERNAME, "your-username") ?: "your-username"
        }
        val token = gitSettingsManager.getGitHubToken().ifEmpty {
            sharedPreferences.getString(KEY_GITHUB_TOKEN, "") ?: ""
        }
        val repoUrl = gitSettingsManager.getRepoUrl().ifEmpty {
            sharedPreferences.getString(KEY_REPO_URL, "https://github.com/your-username/your-hexo-blog.git") ?: "https://github.com/your-username/your-hexo-blog.git"
        }
        
        binding.editGithubUsername.setText(username)
        binding.editGithubToken.setText(token)
        binding.editRepoUrl.setText(repoUrl)
        
        // 加载其他设置
        binding.editBlogTitle.setText(sharedPreferences.getString(KEY_BLOG_TITLE, "我的 Hexo 博客"))
        binding.editAuthorName.setText(sharedPreferences.getString(KEY_AUTHOR_NAME, "Hexo 博客作者"))
        binding.editStoragePath.setText(sharedPreferences.getString(KEY_STORAGE_PATH, "/storage/emulated/0/HexoBlog"))
        
        // 显示 Git 设置状态
        val status = gitSettingsManager.getGitSetupStatus()
        if (!status.isComplete) {
            Toast.makeText(this, "Git 设置不完整，请完善设置", Toast.LENGTH_LONG).show()
        }
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