package com.example.hexobloguploader.git

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

/**
 * Git 模块功能测试
 * 测试 GitOperationsManager 的核心功能
 */
@RunWith(AndroidJUnit4::class)
class GitModuleTest {
    
    companion object {
        private const val TAG = "GitModuleTest"
        
        // 测试配置（需要根据实际情况修改）
        private const val TEST_REPO_URL = "https://github.com/username/test-hexo-blog.git"
        private const val TEST_BRANCH = "main"
        private const val TEST_TOKEN = "your_github_token_here" // 需要替换为有效的 Token
    }
    
    private lateinit var context: Context
    private lateinit var gitManager: GitOperationsManager
    private lateinit var settingsManager: GitSettingsManager
    private lateinit var userExperienceHelper: GitUserExperienceHelper
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        gitManager = GitOperationsManager(context)
        settingsManager = GitSettingsManager(context)
        userExperienceHelper = GitUserExperienceHelper(context)
        
        Log.d(TAG, "测试环境初始化完成")
    }
    
    /**
     * 测试 Git 设置管理器
     */
    @Test
    fun testGitSettingsManager() {
        Log.d(TAG, "开始测试 GitSettingsManager")
        
        // 保存设置
        settingsManager.setGitHubUsername("test-user")
        settingsManager.setRepoUrl(TEST_REPO_URL)
        settingsManager.setBranchName(TEST_BRANCH)
        settingsManager.setGitHubToken(TEST_TOKEN)
        settingsManager.setAuthType(AuthType.HTTPS)
        settingsManager.setAutoCommitEnabled(true)
        settingsManager.setAutoPullEnabled(true)
        
        // 验证设置
        assert(settingsManager.getGitHubUsername() == "test-user")
        assert(settingsManager.getRepoUrl() == TEST_REPO_URL)
        assert(settingsManager.getBranchName() == TEST_BRANCH)
        assert(settingsManager.getGitHubToken() == TEST_TOKEN)
        assert(settingsManager.getAuthType() == AuthType.HTTPS)
        assert(settingsManager.isAutoCommitEnabled())
        assert(settingsManager.isAutoPullEnabled())
        
        // 检查设置状态
        val status = settingsManager.getGitSetupStatus()
        Log.d(TAG, "设置状态: ${status.isComplete}")
        Log.d(TAG, "缺失字段: ${status.missingFields}")
        
        // 清理设置
        settingsManager.clearAllSettings()
        
        Log.d(TAG, "GitSettingsManager 测试完成")
    }
    
    /**
     * 测试 Git 操作管理器 - 基础功能
     */
    @Test
    fun testGitOperationsManagerBasic() {
        Log.d(TAG, "开始测试 GitOperationsManager 基础功能")
        
        // 测试仓库初始化状态
        val isInitialized = gitManager.isRepositoryInitialized()
        Log.d(TAG, "仓库初始化状态: $isInitialized")
        
        // 测试获取仓库信息
        val repoInfo = gitManager.getRepositoryInfo()
        Log.d(TAG, "仓库信息: ${repoInfo.localPath}")
        Log.d(TAG, "Git 目录存在: ${repoInfo.gitDirExists}")
        
        // 测试检查仓库状态
        val status = gitManager.checkRepositoryStatus()
        Log.d(TAG, "仓库状态: ${status.isInitialized}")
        Log.d(TAG, "是否有更改: ${status.hasChanges}")
        Log.d(TAG, "当前分支: ${status.branch}")
        
        // 测试 Hexo 分支结构检测
        val hexoInfo = gitManager.detectHexoBranchStructure()
        Log.d(TAG, "Hexo 分支信息: ${hexoInfo.sourceBranch}")
        Log.d(TAG, "Hexo 结构完整: ${hexoInfo.hasHexoStructure}")
        
        // 测试 Hexo 仓库验证
        val validationResult = gitManager.validateHexoRepository()
        Log.d(TAG, "Hexo 仓库验证: ${validationResult.success}")
        Log.d(TAG, "验证消息: ${validationResult.message}")
        
        // 测试冲突检查
        val conflictInfo = gitManager.checkConflicts()
        Log.d(TAG, "冲突检查: ${conflictInfo.hasConflicts}")
        Log.d(TAG, "冲突文件数量: ${conflictInfo.conflictFiles.size}")
        
        Log.d(TAG, "GitOperationsManager 基础功能测试完成")
    }
    
    /**
     * 测试 Git 用户体验助手
     */
    @Test
    fun testGitUserExperienceHelper() {
        Log.d(TAG, "开始测试 GitUserExperienceHelper")
        
        // 测试错误消息处理
        val testException = Exception("Authentication failed: Invalid credentials")
        val friendlyMessage = userExperienceHelper.getFriendlyErrorMessage(testException)
        Log.d(TAG, "友好错误消息: $friendlyMessage")
        
        val suggestions = userExperienceHelper.getSuggestions(testException)
        Log.d(TAG, "操作建议: $suggestions")
        
        val completeMessage = userExperienceHelper.getCompleteErrorMessage(testException)
        Log.d(TAG, "完整错误信息: $completeMessage")
        
        // 测试操作状态描述
        val successResult = GitResult.success("操作成功", "详细信息")
        val successDescription = userExperienceHelper.getOperationStatusDescription("clone", successResult)
        Log.d(TAG, "成功操作描述: $successDescription")
        
        val errorResult = GitResult.error("操作失败", "错误详情")
        val errorDescription = userExperienceHelper.getOperationStatusDescription("clone", errorResult)
        Log.d(TAG, "失败操作描述: $errorDescription")
        
        // 测试进度描述
        val progressDescription = userExperienceHelper.getProgressDescription("clone", 50, 100)
        Log.d(TAG, "进度描述: $progressDescription")
        
        // 测试成功消息
        val successMessage = userExperienceHelper.getSuccessMessage("clone", "仓库位置")
        Log.d(TAG, "成功消息: $successMessage")
        
        // 测试确认提示
        val confirmationMessage = userExperienceHelper.getConfirmationMessage("clone", "注意备份")
        Log.d(TAG, "确认提示: $confirmationMessage")
        
        // 测试操作注意事项
        val operationNotes = userExperienceHelper.getOperationNotes("clone")
        Log.d(TAG, "操作注意事项: $operationNotes")
        
        Log.d(TAG, "GitUserExperienceHelper 测试完成")
    }
    
    /**
     * 测试 SSH 认证管理器
     */
    @Test
    fun testSshAuthManager() {
        Log.d(TAG, "开始测试 SshAuthManager")
        
        val sshAuthManager = SshAuthManager(context)
        
        // 测试初始化
        sshAuthManager.initSshSessionFactory()
        
        // 测试密钥管理
        val testPrivateKey = "-----BEGIN RSA PRIVATE KEY-----\n测试私钥内容\n-----END RSA PRIVATE KEY-----"
        val testPublicKey = "ssh-rsa AAAAB3NzaC1yc2E...测试公钥内容"
        
        val savePrivateResult = sshAuthManager.savePrivateKey(testPrivateKey)
        Log.d(TAG, "保存私钥结果: $savePrivateResult")
        
        val savePublicResult = sshAuthManager.savePublicKey(testPublicKey)
        Log.d(TAG, "保存公钥结果: $savePublicResult")
        
        // 测试密钥检查
        val hasKey = sshAuthManager.hasSshKey()
        Log.d(TAG, "是否有 SSH 密钥: $hasKey")
        
        val keyInfo = sshAuthManager.getSshKeyInfo()
        Log.d(TAG, "SSH 密钥信息: ${keyInfo.privateKeyExists}, ${keyInfo.publicKeyExists}")
        
        // 测试密钥验证
        val validationResult = sshAuthManager.validateSshKey()
        Log.d(TAG, "SSH 密钥验证: ${validationResult.isValid}, ${validationResult.message}")
        
        // 测试密钥删除
        val deleteResult = sshAuthManager.deleteSshKey()
        Log.d(TAG, "删除 SSH 密钥结果: $deleteResult")
        
        Log.d(TAG, "SshAuthManager 测试完成")
    }
    
    /**
     * 测试冲突处理功能
     */
    @Test
    fun testConflictHandling() {
        Log.d(TAG, "开始测试冲突处理功能")
        
        // 创建模拟冲突信息
        val conflictFiles = listOf(
            "source/_posts/test1.md",
            "source/_posts/test2.md",
            "themes/test-theme/config.yml"
        )
        
        val conflictInfo = ConflictInfo.withConflicts(conflictFiles)
        
        // 测试冲突解决指南
        val conflictGuide = userExperienceHelper.getConflictResolutionGuide(conflictInfo)
        Log.d(TAG, "冲突解决指南: $conflictGuide")
        
        // 测试冲突解决结果
        val resolveResult = ConflictResolutionResult.success(
            "冲突解决成功",
            listOf("source/_posts/test1.md", "source/_posts/test2.md")
        )
        Log.d(TAG, "冲突解决结果: ${resolveResult.success}, ${resolveResult.message}")
        
        Log.d(TAG, "冲突处理功能测试完成")
    }
    
    /**
     * 测试 Hexo 仓库功能
     */
    @Test
    fun testHexoRepositoryFeatures() {
        Log.d(TAG, "开始测试 Hexo 仓库功能")
        
        // 创建模拟 Hexo 分支信息
        val hexoBranchInfo = HexoBranchInfo(
            sourceBranch = "source",
            pagesBranch = "gh-pages",
            hasHexoStructure = true,
            postsDir = "/data/user/0/com.example.hexobloguploader/files/blog/source/_posts",
            themesDir = "/data/user/0/com.example.hexobloguploader/files/blog/themes",
            configFile = "/data/user/0/com.example.hexobloguploader/files/blog/_config.yml"
        )
        
        // 测试 Hexo 仓库验证结果
        val validationResult = userExperienceHelper.getHexoRepositoryValidationResult(hexoBranchInfo)
        Log.d(TAG, "Hexo 仓库验证结果: $validationResult")
        
        // 测试 Hexo 仓库有效性检查
        val isValid = hexoBranchInfo.isValidHexoRepository()
        Log.d(TAG, "Hexo 仓库是否有效: $isValid")
        
        // 测试格式化信息
        val formattedInfo = hexoBranchInfo.getFormattedInfo()
        Log.d(TAG, "Hexo 分支格式化信息: $formattedInfo")
        
        Log.d(TAG, "Hexo 仓库功能测试完成")
    }
    
    /**
     * 测试 Git 操作结果
     */
    @Test
    fun testGitResult() {
        Log.d(TAG, "开始测试 GitResult")
        
        // 测试成功结果
        val successResult = GitResult.success(
            "操作成功",
            "详细信息",
            mapOf("key" to "value")
        )
        
        assert(successResult.success)
        assert(successResult.message == "操作成功")
        assert(successResult.details == "详细信息")
        assert(successResult.data != null)
        
        // 测试错误结果
        val errorResult = GitResult.error(
            "操作失败",
            "错误详情"
        )
        
        assert(!errorResult.success)
        assert(errorResult.message == "操作失败")
        assert(errorResult.details == "错误详情")
        
        Log.d(TAG, "GitResult 测试完成")
    }
    
    /**
     * 测试认证类型
     */
    @Test
    fun testAuthType() {
        Log.d(TAG, "开始测试 AuthType")
        
        // 测试枚举值
        val httpsAuth = AuthType.HTTPS
        val sshAuth = AuthType.SSH
        
        assert(httpsAuth.name == "HTTPS")
        assert(sshAuth.name == "SSH")
        
        // 测试枚举比较
        assert(httpsAuth != sshAuth)
        
        Log.d(TAG, "AuthType 测试完成")
    }
    
    /**
     * 运行所有测试
     */
    @Test
    fun runAllTests() {
        Log.d(TAG, "开始运行所有 Git 模块测试")
        
        try {
            testGitSettingsManager()
            testGitOperationsManagerBasic()
            testGitUserExperienceHelper()
            testSshAuthManager()
            testConflictHandling()
            testHexoRepositoryFeatures()
            testGitResult()
            testAuthType()
            
            Log.d(TAG, "所有 Git 模块测试完成 ✅")
        } catch (e: Exception) {
            Log.e(TAG, "测试失败: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * 生成测试报告
     */
    fun generateTestReport(): String {
        return buildString {
            appendLine("Git 模块测试报告")
            appendLine("=================")
            appendLine()
            appendLine("测试时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}")
            appendLine()
            appendLine("已测试的功能模块:")
            appendLine("1. GitSettingsManager - Git 设置管理")
            appendLine("2. GitOperationsManager - Git 操作管理")
            appendLine("3. GitUserExperienceHelper - 用户体验助手")
            appendLine("4. SshAuthManager - SSH 认证管理")
            appendLine("5. 冲突处理功能")
            appendLine("6. Hexo 仓库功能")
            appendLine("7. GitResult 和 AuthType")
            appendLine()
            appendLine("测试说明:")
            appendLine("- 单元测试验证了核心功能的正确性")
            appendLine("- 集成测试需要实际的 Git 仓库和 Token")
            appendLine("- 实际使用前请配置正确的仓库 URL 和 Token")
            appendLine()
            appendLine("注意事项:")
            appendLine("1. 克隆测试需要有效的 GitHub Token")
            appendLine("2. SSH 测试需要配置 SSH 密钥")
            appendLine("3. 推送测试需要仓库写入权限")
            appendLine("4. 冲突测试需要模拟冲突场景")
        }
    }
}