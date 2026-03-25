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
class GitManagerActivity : AppCompatActivity() {
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
            cloneMyBlogRepository()
        }
        
        binding.buttonSetupActions.setOnClickListener {
            setupGitHubActions()
        }
        
        binding.buttonCheckActions.setOnClickListener {
            checkGitHubActionsStatus()
        }
    }
    
    private fun checkRepositoryStatus() {
        appendLog("🔍 检查仓库状态...")
        
        val status = gitManager.checkRepositoryStatus()
        
        binding.textRepositoryInfo.text = status.getFormattedStatus()
        
        if (status.isInitialized) {
            appendLog("✅ 仓库已初始化")
            appendLog("   分支: ${status.branch}")
            appendLog("   最后提交: ${status.lastCommitMessage}")
            appendLog("   更改文件: ${status.getChangeCount()} 个")
            
            // 更新按钮状态
            binding.buttonPullUpdates.isEnabled = true
            binding.buttonCommitPush.isEnabled = status.hasChanges
            binding.buttonResetChanges.isEnabled = status.hasChanges
        } else {
            appendLog("❌ 仓库未初始化")
            appendLog("   错误: ${status.error}")
            
            // 更新按钮状态
            binding.buttonPullUpdates.isEnabled = false
            binding.buttonCommitPush.isEnabled = false
            binding.buttonResetChanges.isEnabled = false
        }
    }
    
    private fun showCloneDialog() {
        // 简化版本：直接使用测试URL和Token
        // 实际应用中应该显示一个对话框让用户输入
        cloneRepository(testRepoUrl, testToken)
    }
    
    private fun cloneRepository(repoUrl: String, token: String) {
        if (repoUrl.isEmpty() || token.isEmpty()) {
            appendLog("❌ 请输入仓库URL和Token")
            return
        }
        
        appendLog("🚀 开始克隆仓库...")
        appendLog("   仓库: $repoUrl")
        appendLog("   分支: ${GitOperationsManager.DEFAULT_BRANCH}")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor { message, percent ->
            runOnUiThread {
                binding.textProgress.text = message
                binding.progressBar.progress = percent
            }
        }
        
        // 在后台执行克隆操作
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.cloneRepository(repoUrl, token, progressMonitor)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    appendLog("   路径: ${result.details}")
                    
                    binding.textProgress.text = "克隆完成"
                    binding.progressBar.progress = 100
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                    checkRepositoryStatus()
                    
                    Toast.makeText(this@GitManagerActivity, "仓库克隆成功", Toast.LENGTH_SHORT).show()
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.details}")
                    
                    binding.textProgress.text = "克隆失败"
                    binding.progressBar.progress = 0
                    
                    Toast.makeText(this@GitManagerActivity, "仓库克隆失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun pullUpdates() {
        appendLog("⬇️ 开始拉取更新...")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor { message, percent ->
            runOnUiThread {
                binding.textProgress.text = message
                binding.progressBar.progress = percent
            }
        }
        
        // 在后台执行拉取操作
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.pullUpdates(testToken, progressMonitor)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    
                    binding.textProgress.text = "拉取完成"
                    binding.progressBar.progress = 100
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                    checkRepositoryStatus()
                    
                    Toast.makeText(this@GitManagerActivity, "更新拉取成功", Toast.LENGTH_SHORT).show()
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.details}")
                    
                    binding.textProgress.text = "拉取失败"
                    binding.progressBar.progress = 0
                    
                    Toast.makeText(this@GitManagerActivity, "更新拉取失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showCommitDialog() {
        // 简化版本：使用固定的提交信息
        // 实际应用中应该显示一个对话框让用户输入提交信息
        val commitMessage = "更新博客文章 - ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
        commitAndPush(commitMessage, testToken)
    }
    
    private fun commitAndPush(message: String, token: String) {
        if (message.isEmpty()) {
            appendLog("❌ 请输入提交信息")
            return
        }
        
        appendLog("💾 开始提交并推送...")
        appendLog("   提交信息: $message")
        
        // 在后台执行提交操作
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.commitAndPush(message, token)
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    appendLog("   提交ID: ${result.details}")
                    
                    binding.textProgress.text = "提交完成"
                    
                    // 更新仓库信息
                    checkRepositoryStatus()
                    
                    Toast.makeText(this@GitManagerActivity, "提交并推送成功", Toast.LENGTH_SHORT).show()
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.details}")
                    
                    binding.textProgress.text = "提交失败"
                    
                    Toast.makeText(this@GitManagerActivity, "提交失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun resetChanges() {
        appendLog("↩️ 开始重置更改...")
        
        // 在后台执行重置操作
        CoroutineScope(Dispatchers.IO).launch {
            val result = gitManager.resetChanges()
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    appendLog("   重置类型: ${result.details}")
                    
                    binding.textProgress.text = "重置完成"
                    
                    // 更新仓库信息
                    checkRepositoryStatus()
                    
                    Toast.makeText(this@GitManagerActivity, "重置成功", Toast.LENGTH_SHORT).show()
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.details}")
                    
                    binding.textProgress.text = "重置失败"
                    
                    Toast.makeText(this@GitManagerActivity, "重置失败: ${result.message}", Toast.LENGTH_LONG).show()
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
        
        cloneRepository(testRepo, testToken)
    }
    
    private fun cloneMyBlogRepository() {
        appendLog("🚀 开始克隆你的 Hexo 博客仓库...")
        
        // 询问用户使用哪种认证方式
        showAuthMethodDialog()
    }
    
    private fun showAuthMethodDialog() {
        // 这里应该显示一个对话框让用户选择认证方式
        // 为了简化，我们先尝试 HTTPS 方式
        
        appendLog("🔐 请选择认证方式:")
        appendLog("   1. HTTPS + Personal Access Token（推荐）")
        appendLog("   2. SSH + 密钥（需要配置 SSH 密钥）")
        
        // 默认使用 HTTPS 方式，因为更简单
        useHttpsForMyBlog()
    }
    
    private fun useHttpsForMyBlog() {
        appendLog("📝 使用 HTTPS 协议克隆...")
        
        // HTTPS 地址
        val httpsRepoUrl = "https://github.com/hongming351/hongming351.github.io.git"
        appendLog("   仓库: $httpsRepoUrl")
        appendLog("   分支: ${GitOperationsManager.DEFAULT_BRANCH}")
        appendLog("   需要: GitHub Personal Access Token")
        
        // 这里应该显示一个对话框让用户输入 Token
        // 为了测试，我们先使用一个示例 Token（实际使用时需要用户输入）
        val testToken = "ghp_your_personal_access_token_here"
        
        if (testToken.startsWith("ghp_")) {
            cloneRepositoryWithToken(httpsRepoUrl, testToken)
        } else {
            appendLog("❌ 请提供有效的 GitHub Personal Access Token")
            appendLog("💡 如何获取 Token:")
            appendLog("   1. 访问: https://github.com/settings/tokens")
            appendLog("   2. 点击 'Generate new token'")
            appendLog("   3. 选择 'repo' 权限")
            appendLog("   4. 复制以 'ghp_' 开头的 Token")
            
            // 在实际应用中，这里应该显示一个输入框让用户输入 Token
            appendLog("📱 请在应用中输入你的 Token")
        }
    }
    
    private fun cloneRepositoryWithToken(repoUrl: String, token: String) {
        appendLog("🔑 使用 Token 克隆仓库...")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor { message, percent ->
            runOnUiThread {
                binding.textProgress.text = message
                binding.progressBar.progress = percent
            }
        }
        
        // 在后台执行克隆操作
        CoroutineScope(Dispatchers.IO).launch {
            // 使用 HTTPS 协议克隆
            val result = gitManager.cloneRepository(
                repoUrl = repoUrl,
                authToken = token,
                authType = com.example.hexobloguploader.git.AuthType.HTTPS,
                progressMonitor = progressMonitor
            )
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    appendLog("   路径: ${result.details}")
                    
                    binding.textProgress.text = "克隆完成"
                    binding.progressBar.progress = 100
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                    checkRepositoryStatus()
                    
                    Toast.makeText(this@GitManagerActivity, "博客仓库克隆成功", Toast.LENGTH_SHORT).show()
                    
                    // 显示下一步提示
                    appendLog("🎉 克隆成功！")
                    appendLog("📝 下一步:")
                    appendLog("   1. 返回主界面查看博客列表")
                    appendLog("   2. 点击博客查看文章详情")
                    appendLog("   3. 编辑文章并提交推送")
                    appendLog("   4. 定期拉取更新保持同步")
                    
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.details}")
                    
                    binding.textProgress.text = "克隆失败"
                    binding.progressBar.progress = 0
                    
                    // 提供解决方案
                    appendLog("💡 解决方案:")
                    appendLog("   1. 检查 Token 是否正确（以 'ghp_' 开头）")
                    appendLog("   2. 确保 Token 有 'repo' 权限")
                    appendLog("   3. 检查网络连接")
                    appendLog("   4. 验证仓库地址是否正确")
                    
                    Toast.makeText(this@GitManagerActivity, "博客仓库克隆失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun useSshForMyBlog() {
        appendLog("🔐 使用 SSH 协议克隆...")
        
        // SSH 地址
        val sshRepoUrl = "git@github.com:hongming351/hongming351.github.io.git"
        appendLog("   仓库: $sshRepoUrl")
        appendLog("   分支: ${GitOperationsManager.DEFAULT_BRANCH}")
        appendLog("   需要: SSH 密钥配置")
        
        // 创建进度监视器
        val progressMonitor = SimpleProgressMonitor { message, percent ->
            runOnUiThread {
                binding.textProgress.text = message
                binding.progressBar.progress = percent
            }
        }
        
        // 在后台执行克隆操作
        CoroutineScope(Dispatchers.IO).launch {
            // 使用 SSH 协议克隆
            val result = gitManager.cloneRepository(
                repoUrl = sshRepoUrl,
                authToken = null, // SSH 需要配置密钥
                authType = com.example.hexobloguploader.git.AuthType.SSH,
                progressMonitor = progressMonitor
            )
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    appendLog("   路径: ${result.details}")
                    
                    binding.textProgress.text = "克隆完成"
                    binding.progressBar.progress = 100
                    
                    // 更新仓库信息
                    updateRepositoryInfo()
                    checkRepositoryStatus()
                    
                    Toast.makeText(this@GitManagerActivity, "博客仓库克隆成功", Toast.LENGTH_SHORT).show()
                    
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.details}")
                    
                    binding.textProgress.text = "克隆失败"
                    binding.progressBar.progress = 0
                    
                    // 提供解决方案
                    appendLog("💡 SSH 密钥配置指南:")
                    appendLog("   1. 在电脑上生成 SSH 密钥:")
                    appendLog("      ssh-keygen -t ed25519 -C 'your_email@example.com'")
                    appendLog("   2. 添加公钥到 GitHub:")
                    appendLog("      访问: https://github.com/settings/keys")
                    appendLog("      点击 'New SSH key'")
                    appendLog("      粘贴 ~/.ssh/id_ed25519.pub 内容")
                    appendLog("   3. 或者使用 HTTPS 方式（更简单）")
                    
                    Toast.makeText(this@GitManagerActivity, "SSH 克隆失败，请配置 SSH 密钥或使用 HTTPS", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun updateRepositoryInfo() {
        val repoInfo = gitManager.getRepositoryInfo()
        
        binding.textRepositoryInfo.text = """
            📁 仓库信息:
            
            本地路径: ${repoInfo.localPath}
            Git目录: ${if (repoInfo.gitDirExists) "✅ 存在" else "❌ 不存在"}
            Git路径: ${repoInfo.gitDirPath}
            
            文章数量: ${repoInfo.storageInfo.postCount}
            存储大小: ${repoInfo.storageInfo.getFormattedSize()}
            
            状态: ${if (gitManager.isRepositoryInitialized()) "✅ 已初始化" else "❌ 未初始化"}
        """.trimIndent()
    }
    
    /**
     * 设置 GitHub Actions 自动部署
     */
    private fun setupGitHubActions() {
        appendLog("⚙️ 开始配置 GitHub Actions...")
        
        if (!gitManager.isRepositoryInitialized()) {
            appendLog("❌ 请先克隆博客仓库")
            Toast.makeText(this, "请先克隆博客仓库", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 在后台执行配置生成
        CoroutineScope(Dispatchers.IO).launch {
            val configGenerator = com.example.hexobloguploader.config.GitHubActionsConfigGenerator(this@GitManagerActivity)
            val result = configGenerator.generateConfigFiles(
                blogTitle = "我的 Hexo 博客",
                authorName = "Hexo 博客作者",
                githubUsername = "your-username",
                repoName = "your-username.github.io"
            )
            
            withContext(Dispatchers.Main) {
                if (result.success) {
                    appendLog("✅ ${result.message}")
                    appendLog("   工作流文件: ${result.workflowPath}")
                    appendLog("   package.json: ${result.packageJsonPath}")
                    appendLog("   _config.yml: ${result.configYmlPath}")
                    
                    // 显示配置说明
                    appendLog("📋 GitHub Actions 配置说明:")
                    appendLog("   1. 工作流文件已创建: .github/workflows/deploy.yml")
                    appendLog("   2. Hexo 配置文件已创建: _config.yml")
                    appendLog("   3. Node.js 依赖文件已创建: package.json")
                    appendLog("   4. 必要的目录结构已创建")
                    
                    appendLog("🚀 下一步:")
                    appendLog("   1. 提交并推送这些配置文件到 GitHub")
                    appendLog("   2. 在 GitHub 仓库的 Settings → Pages 中启用 GitHub Pages")
                    appendLog("   3. 推送文章到 source 分支")
                    appendLog("   4. GitHub Actions 会自动构建并部署到 gh-pages 分支")
                    
                    Toast.makeText(this@GitManagerActivity, "GitHub Actions 配置成功", Toast.LENGTH_SHORT).show()
                    
                    // 检查配置状态
                    checkGitHubActionsStatus()
                    
                } else {
                    appendLog("❌ ${result.message}")
                    appendLog("   错误: ${result.error}")
                    
                    Toast.makeText(this@GitManagerActivity, "GitHub Actions 配置失败: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    /**
     * 检查 GitHub Actions 配置状态
     */
    private fun checkGitHubActionsStatus() {
        appendLog("🔍 检查 GitHub Actions 配置状态...")
        
        if (!gitManager.isRepositoryInitialized()) {
            appendLog("❌ 仓库未初始化")
            return
        }
        
        val configGenerator = com.example.hexobloguploader.config.GitHubActionsConfigGenerator(this)
        val status = configGenerator.getConfigStatus()
        
        appendLog(status.getFormattedStatus())
        
        if (status.isFullyConfigured) {
            appendLog("🎉 GitHub Actions 已完全配置！")
            appendLog("📝 使用方法:")
            appendLog("   1. 编写文章并保存")
            appendLog("   2. 提交并推送到 source 分支")
            appendLog("   3. GitHub Actions 会自动构建并部署")
            appendLog("   4. 访问 https://your-username.github.io 查看博客")
        } else {
            appendLog("⚠️ GitHub Actions 配置不完整")
            appendLog("💡 建议:")
            appendLog("   1. 点击 '设置 GitHub Actions' 按钮生成配置文件")
            appendLog("   2. 或者手动创建缺失的文件")
            appendLog("   3. 确保所有配置文件都存在")
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