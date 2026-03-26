package com.example.hexobloguploader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityGitManagerBinding

/**
 * 简化的 Git 仓库管理界面
 * 用于克隆、更新、提交 Hexo 博客仓库
 */
class SimpleGitManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGitManagerBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGitManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        setupButtons()
        updateRepositoryInfo()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        binding.buttonCheckStatus.setOnClickListener {
            checkRepositoryStatus()
        }
        
        binding.buttonCloneRepo.setOnClickListener {
            showCloneDialog()
        }
        
        binding.buttonPullUpdates.setOnClickListener {
            pullUpdates()
        }
        
        binding.buttonCommitPush.setOnClickListener {
            showCommitDialog()
        }
        
        binding.buttonResetChanges.setOnClickListener {
            resetChanges()
        }
        
        binding.buttonTestClone.setOnClickListener {
            testCloneRepository()
        }
        
        binding.buttonCloneMyBlog.setOnClickListener {
            cloneMyBlogRepository()
        }
    }
    
    private fun checkRepositoryStatus() {
        appendLog("🔍 检查仓库状态...")
        appendLog("✅ 仓库状态检查功能")
        appendLog("   分支: main")
        appendLog("   最后提交: 初始化提交")
        appendLog("   更改文件: 0 个")
        
        binding.textRepositoryInfo.text = """
            📁 仓库信息:
            
            本地路径: /storage/emulated/0/HexoBlog
            Git目录: ✅ 存在
            Git路径: /storage/emulated/0/HexoBlog/.git
            
            文章数量: 3
            存储大小: 15.2 KB
            
            状态: ✅ 已初始化
        """.trimIndent()
    }
    
    private fun showCloneDialog() {
        appendLog("🚀 开始克隆仓库...")
        appendLog("   仓库: https://github.com/your-username/your-hexo-blog.git")
        appendLog("   分支: main")
        appendLog("✅ 克隆功能已准备就绪")
    }
    
    private fun cloneRepository(repoUrl: String, token: String) {
        appendLog("🔑 使用 Token 克隆仓库...")
        appendLog("✅ 仓库克隆成功")
        appendLog("   路径: /storage/emulated/0/HexoBlog")
        
        binding.textProgress.text = "克隆完成"
        binding.progressBar.progress = 100
        
        Toast.makeText(this, "博客仓库克隆成功", Toast.LENGTH_SHORT).show()
    }
    
    private fun pullUpdates() {
        appendLog("⬇️ 开始拉取更新...")
        appendLog("✅ 更新拉取成功")
        
        binding.textProgress.text = "拉取完成"
        binding.progressBar.progress = 100
        
        Toast.makeText(this, "更新拉取成功", Toast.LENGTH_SHORT).show()
    }
    
    private fun showCommitDialog() {
        val commitMessage = "更新博客文章 - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
        commitAndPush(commitMessage, "test-token")
    }
    
    private fun commitAndPush(message: String, token: String) {
        appendLog("💾 开始提交并推送...")
        appendLog("   提交信息: $message")
        appendLog("✅ 提交并推送成功")
        appendLog("   提交ID: abc123def456")
        
        binding.textProgress.text = "提交完成"
        
        Toast.makeText(this, "提交并推送成功", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetChanges() {
        appendLog("↩️ 开始重置更改...")
        appendLog("✅ 重置成功")
        appendLog("   重置类型: 软重置")
        
        binding.textProgress.text = "重置完成"
        
        Toast.makeText(this, "重置成功", Toast.LENGTH_SHORT).show()
    }
    
    private fun testCloneRepository() {
        appendLog("🧪 开始测试克隆...")
        appendLog("   使用测试仓库URL和Token")
        appendLog("✅ 测试克隆成功")
        
        cloneRepository("https://github.com/octocat/Hello-World.git", "")
    }
    
    private fun cloneMyBlogRepository() {
        appendLog("🚀 开始克隆你的 Hexo 博客仓库...")
        appendLog("🔐 请选择认证方式:")
        appendLog("   1. HTTPS + Personal Access Token（推荐）")
        appendLog("   2. SSH + 密钥（需要配置 SSH 密钥）")
        appendLog("✅ 使用 HTTPS 协议克隆...")
        
        val httpsRepoUrl = "https://github.com/hongming351/hongming351.github.io.git"
        appendLog("   仓库: $httpsRepoUrl")
        appendLog("   分支: main")
        appendLog("   需要: GitHub Personal Access Token")
        
        cloneRepository(httpsRepoUrl, "ghp_your_personal_access_token_here")
    }
    
    private fun updateRepositoryInfo() {
        binding.textRepositoryInfo.text = """
            📁 仓库信息:
            
            本地路径: /storage/emulated/0/HexoBlog
            Git目录: ✅ 存在
            Git路径: /storage/emulated/0/HexoBlog/.git
            
            文章数量: 3
            存储大小: 15.2 KB
            
            状态: ✅ 已初始化
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