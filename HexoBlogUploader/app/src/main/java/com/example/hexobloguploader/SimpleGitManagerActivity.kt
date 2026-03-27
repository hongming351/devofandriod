package com.example.hexobloguploader

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hexobloguploader.databinding.ActivityGitManagerBinding
import com.example.hexobloguploader.git.GitOperationsManager
import com.example.hexobloguploader.git.SimpleProgressMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Git 仓库管理界面
 * 用于克隆、更新、提交 Hexo 博客仓库
 */
class SimpleGitManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGitManagerBinding
    private lateinit var gitManager: GitOperationsManager
    
    // 测试用的仓库URL和Token（实际使用时应该从设置或输入框获取）
    private val testRepoUrl = "https://github.com/your-username/your-hexo-blog.git"
    private val testToken = "ghp_your_personal_access_token_here"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGitManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        gitManager = GitOperationsManager(this)
        
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
            showCloneMyBlogDialog()
        }
        
        // 启用按钮
        binding.buttonPullUpdates.isEnabled = true
        binding.buttonCommitPush.isEnabled = true
        binding.buttonResetChanges.isEnabled = true
    }
    
    private fun checkRepositoryStatus() {
        appendLog("🔍 检查仓库状态...")
        
        CoroutineScope(Dispatchers.IO).launch {
            val status = gitManager.checkRepositoryStatus()
            
            withContext(Dispatchers.Main) {
                if (status.isInitialized) {
                    appendLog("✅ 仓库状态:")
                    appendLog("   分支: ${status.branch ?: "未知"}")
                    appendLog("   最后提交: ${status.lastCommitMessage ?: "无"}")
                    appendLog("   作者: ${status.lastCommitAuthor ?: "未知"}")
                    appendLog("   时间: ${status.lastCommitDate?.toString() ?: "未知"}")
                    appendLog("   更改文件: ${status.getChangeCount()} 个")
                    
                    if (status.addedFiles.isNotEmpty()) {
                        appendLog("   新增: ${status.addedFiles.size}")
                    }
                    if (status.modifiedFiles.isNotEmpty()) {
                        appendLog("   修改: ${status.modifiedFiles.size}")
                    }
                    if (status.untrackedFiles.isNotEmpty()) {
                        appendLog("   未跟踪: ${status.untrackedFiles.size}")
                    }
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 仓库未初始化")
                    appendLog("   错误: ${status.error ?: "未知错误"}")
                }
            }
        }
    }
    
    private fun showCloneDialog() {
        appendLog("🚀 开始克隆仓库...")
        appendLog("   仓库: $testRepoUrl")
        appendLog("   分支: ${GitOperationsManager.DEFAULT_BRANCH}")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor("克隆进度") { message, progress ->
            binding.textProgress.text = message
            binding.progressBar.progress = progress
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.cloneRepository(testRepoUrl, testToken, progressMonitor = progressMonitor)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 仓库克隆成功")
                    appendLog("   路径: ${result.details}")
                    Toast.makeText(this@SimpleGitManagerActivity, "博客仓库克隆成功", Toast.LENGTH_SHORT).show()
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 仓库克隆失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "克隆失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun pullUpdates() {
        appendLog("⬇️ 开始拉取更新...")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor("拉取进度") { message, progress ->
            binding.textProgress.text = message
            binding.progressBar.progress = progress
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.pullUpdates(testToken, progressMonitor)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 更新拉取成功")
                    appendLog("   路径: ${result.details}")
                    Toast.makeText(this@SimpleGitManagerActivity, "更新拉取成功", Toast.LENGTH_SHORT).show()
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 更新拉取失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "拉取失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showCommitDialog() {
        val commitMessage = "更新博客文章 - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
        commitAndPush(commitMessage, testToken)
    }
    
    private fun commitAndPush(message: String, token: String) {
        appendLog("💾 开始提交并推送...")
        appendLog("   提交信息: $message")
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.commitAndPush(message, token)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 提交并推送成功")
                    appendLog("   提交ID: ${result.details}")
                    binding.textProgress.text = "提交完成"
                    Toast.makeText(this@SimpleGitManagerActivity, "提交并推送成功", Toast.LENGTH_SHORT).show()
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 提交失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "提交失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun resetChanges() {
        appendLog("↩️ 开始重置更改...")
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.resetChanges()
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 重置成功")
                    appendLog("   重置类型: ${result.details}")
                    binding.textProgress.text = "重置完成"
                    Toast.makeText(this@SimpleGitManagerActivity, "重置成功", Toast.LENGTH_SHORT).show()
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 重置失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "重置失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun testCloneRepository() {
        appendLog("🧪 开始测试克隆...")
        appendLog("   使用测试仓库URL和Token")
        
        // 这里可以使用一个公开的测试仓库
        val testRepo = "https://github.com/octocat/Hello-World.git"
        val testToken = "" // 公开仓库不需要Token
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor("测试克隆进度") { message, progress ->
            binding.textProgress.text = message
            binding.progressBar.progress = progress
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.cloneRepository(testRepo, testToken, progressMonitor = progressMonitor)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 测试克隆成功")
                    Toast.makeText(this@SimpleGitManagerActivity, "测试克隆成功", Toast.LENGTH_SHORT).show()
                } else {
                    appendLog("❌ 测试克隆失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "测试克隆失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 显示克隆我的博客对话框
     */
    private fun showCloneMyBlogDialog() {
        android.app.AlertDialog.Builder(this)
            .setTitle("克隆我的 Hexo 博客")
            .setMessage("这个功能将克隆你的 Hexo 博客仓库。\n\n请确保:\n1. 你有 GitHub Personal Access Token\n2. 你知道你的仓库地址\n3. 你有仓库的访问权限")
            .setPositiveButton("使用默认设置") { _, _ ->
                cloneMyBlogWithDefaultSettings()
            }
            .setNegativeButton("自定义设置") { _, _ ->
                showCustomCloneDialog()
            }
            .setNeutralButton("取消", null)
            .show()
    }
    
    /**
     * 使用默认设置克隆我的博客
     */
    private fun cloneMyBlogWithDefaultSettings() {
        appendLog("🚀 开始克隆你的 Hexo 博客仓库...")
        appendLog("🔐 使用默认设置...")
        
        // 默认仓库地址
        val defaultRepoUrl = "https://github.com/hongming351/hongming351.github.io.git"
        appendLog("   仓库: $defaultRepoUrl")
        appendLog("   分支: ${GitOperationsManager.DEFAULT_BRANCH}")
        appendLog("   需要: GitHub Personal Access Token")
        
        // 这里应该从设置中获取Token
        val token = getSavedToken()
        
        if (token.isEmpty() || !token.startsWith("ghp_")) {
            appendLog("❌ 请先在设置中配置有效的 GitHub Personal Access Token")
            appendLog("💡 如何获取 Token:")
            appendLog("   1. 访问: https://github.com/settings/tokens")
            appendLog("   2. 点击 'Generate new token'")
            appendLog("   3. 选择 'repo' 权限")
            appendLog("   4. 复制以 'ghp_' 开头的 Token")
            appendLog("   5. 在应用设置中保存 Token")
            
            // 提示用户去设置
            android.app.AlertDialog.Builder(this)
                .setTitle("需要配置 Token")
                .setMessage("请先在设置中配置 GitHub Personal Access Token")
                .setPositiveButton("打开设置") { _, _ ->
                    val intent = android.content.Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor("克隆进度") { message, progress ->
            binding.textProgress.text = message
            binding.progressBar.progress = progress
        }
        
        // 使用 HTTPS 协议克隆
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.cloneRepository(
                repoUrl = defaultRepoUrl,
                authToken = token,
                progressMonitor = progressMonitor
            )
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 博客仓库克隆成功")
                    appendLog("   路径: ${result.details}")
                    Toast.makeText(this@SimpleGitManagerActivity, "博客仓库克隆成功", Toast.LENGTH_SHORT).show()
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 博客仓库克隆失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "博客仓库克隆失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 显示自定义克隆对话框
     */
    private fun showCustomCloneDialog() {
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(32, 16, 32, 16)
        
        val editRepoUrl = android.widget.EditText(this)
        editRepoUrl.hint = "仓库URL (例如: https://github.com/username/repo.git)"
        editRepoUrl.setText("https://github.com/hongming351/hongming351.github.io.git")
        
        val editToken = android.widget.EditText(this)
        editToken.hint = "GitHub Personal Access Token (以 ghp_ 开头)"
        editToken.setText(getSavedToken())
        
        val editBranch = android.widget.EditText(this)
        editBranch.hint = "分支 (默认: ${GitOperationsManager.DEFAULT_BRANCH})"
        editBranch.setText(GitOperationsManager.DEFAULT_BRANCH)
        
        layout.addView(editRepoUrl)
        layout.addView(editToken)
        layout.addView(editBranch)
        
        android.app.AlertDialog.Builder(this)
            .setTitle("自定义克隆设置")
            .setView(layout)
            .setPositiveButton("开始克隆") { _, _ ->
                val repoUrl = editRepoUrl.text.toString().trim()
                val token = editToken.text.toString().trim()
                val branch = editBranch.text.toString().trim().ifEmpty { GitOperationsManager.DEFAULT_BRANCH }
                
                if (repoUrl.isEmpty()) {
                    Toast.makeText(this, "请输入仓库URL", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                if (token.isEmpty() || !token.startsWith("ghp_")) {
                    Toast.makeText(this, "请输入有效的 GitHub Token", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                cloneRepositoryCustom(repoUrl, token, branch)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 自定义克隆仓库
     */
    private fun cloneRepositoryCustom(repoUrl: String, token: String, branch: String) {
        appendLog("🚀 开始克隆仓库...")
        appendLog("   仓库: $repoUrl")
        appendLog("   分支: $branch")
        appendLog("   认证: 使用 Token")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor("克隆进度") { message, progress ->
            binding.textProgress.text = message
            binding.progressBar.progress = progress
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            // 使用新的支持分支的方法
            val result = gitManager.cloneRepositoryWithBranch(
                repoUrl = repoUrl,
                branch = branch,
                authToken = token,
                progressMonitor = progressMonitor
            )
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ 仓库克隆成功")
                    appendLog("   路径: ${result.details}")
                    Toast.makeText(this@SimpleGitManagerActivity, "仓库克隆成功", Toast.LENGTH_SHORT).show()
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                } else {
                    appendLog("❌ 仓库克隆失败")
                    appendLog("   错误: ${result.message}")
                    Toast.makeText(this@SimpleGitManagerActivity, "克隆失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 获取保存的 Token
     */
    private fun getSavedToken(): String {
        // 这里应该从 SharedPreferences 或安全存储中获取 Token
        // 暂时返回测试 Token
        return testToken
    }
    
    private fun updateRepositoryInfo() {
        CoroutineScope(Dispatchers.IO).launch {
            val repoInfo = gitManager.getRepositoryInfo()
            val isInitialized = gitManager.isRepositoryInitialized()
            
            withContext(Dispatchers.Main) {
                binding.textRepositoryInfo.text = """
                    📁 仓库信息:
                    
                    本地路径: ${repoInfo.localPath}
                    Git目录: ${if (repoInfo.gitDirExists) "✅ 存在" else "❌ 不存在"}
                    Git路径: ${repoInfo.gitDirPath}
                    
                    文章数量: ${repoInfo.storageInfo.postCount}
                    存储大小: ${repoInfo.storageInfo.getFormattedSize()}
                    
                    状态: ${if (isInitialized) "✅ 已初始化" else "❌ 未初始化"}
                """.trimIndent()
            }
        }
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