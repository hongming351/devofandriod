package com.example.hexobloguploader.git

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Git 设置管理器
 * 负责管理 Git 相关的设置和配置
 */
class GitSettingsManager(private val context: Context) {
    
    companion object {
        private const val TAG = "GitSettingsManager"
        private const val PREFS_NAME = "hexo_blog_git_settings"
        
        // 设置键名
        const val KEY_GITHUB_TOKEN = "github_token"
        const val KEY_REPO_URL = "repo_url"
        const val KEY_GITHUB_USERNAME = "github_username"
        const val KEY_AUTO_COMMIT = "auto_commit_enabled"
        const val KEY_AUTO_PULL = "auto_pull_enabled"
        const val KEY_CONFLICT_RESOLUTION = "conflict_resolution"
        const val KEY_BRANCH_NAME = "branch_name"
        const val KEY_AUTH_TYPE = "auth_type"
        
        // 默认值
        const val DEFAULT_BRANCH = "source"
        const val DEFAULT_AUTH_TYPE = "HTTPS"
        const val DEFAULT_CONFLICT_RESOLUTION = "USE_LOCAL"
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * 获取 GitHub Token
     */
    fun getGitHubToken(): String {
        return sharedPreferences.getString(KEY_GITHUB_TOKEN, "") ?: ""
    }
    
    /**
     * 设置 GitHub Token
     */
    fun setGitHubToken(token: String) {
        sharedPreferences.edit()
            .putString(KEY_GITHUB_TOKEN, token)
            .apply()
        Log.d(TAG, "GitHub Token 已保存")
    }
    
    /**
     * 获取仓库 URL
     */
    fun getRepoUrl(): String {
        return sharedPreferences.getString(KEY_REPO_URL, "") ?: ""
    }
    
    /**
     * 设置仓库 URL
     */
    fun setRepoUrl(url: String) {
        sharedPreferences.edit()
            .putString(KEY_REPO_URL, url)
            .apply()
        Log.d(TAG, "仓库 URL 已保存: $url")
    }
    
    /**
     * 获取 GitHub 用户名
     */
    fun getGitHubUsername(): String {
        return sharedPreferences.getString(KEY_GITHUB_USERNAME, "") ?: ""
    }
    
    /**
     * 设置 GitHub 用户名
     */
    fun setGitHubUsername(username: String) {
        sharedPreferences.edit()
            .putString(KEY_GITHUB_USERNAME, username)
            .apply()
        Log.d(TAG, "GitHub 用户名已保存: $username")
    }
    
    /**
     * 检查是否启用自动提交
     */
    fun isAutoCommitEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_COMMIT, true)
    }
    
    /**
     * 设置自动提交启用状态
     */
    fun setAutoCommitEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_AUTO_COMMIT, enabled)
            .apply()
        Log.d(TAG, "自动提交设置已更新: $enabled")
    }
    
    /**
     * 检查是否启用自动拉取
     */
    fun isAutoPullEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_AUTO_PULL, true)
    }
    
    /**
     * 设置自动拉取启用状态
     */
    fun setAutoPullEnabled(enabled: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_AUTO_PULL, enabled)
            .apply()
        Log.d(TAG, "自动拉取设置已更新: $enabled")
    }
    
    /**
     * 获取冲突解决策略
     */
    fun getConflictResolution(): ConflictResolution {
        val resolutionStr = sharedPreferences.getString(KEY_CONFLICT_RESOLUTION, DEFAULT_CONFLICT_RESOLUTION) ?: DEFAULT_CONFLICT_RESOLUTION
        return try {
            ConflictResolution.valueOf(resolutionStr)
        } catch (e: IllegalArgumentException) {
            ConflictResolution.USE_LOCAL
        }
    }
    
    /**
     * 设置冲突解决策略
     */
    fun setConflictResolution(resolution: ConflictResolution) {
        sharedPreferences.edit()
            .putString(KEY_CONFLICT_RESOLUTION, resolution.name)
            .apply()
        Log.d(TAG, "冲突解决策略已更新: ${resolution.name}")
    }
    
    /**
     * 获取分支名称
     */
    fun getBranchName(): String {
        return sharedPreferences.getString(KEY_BRANCH_NAME, DEFAULT_BRANCH) ?: DEFAULT_BRANCH
    }
    
    /**
     * 设置分支名称
     */
    fun setBranchName(branchName: String) {
        sharedPreferences.edit()
            .putString(KEY_BRANCH_NAME, branchName)
            .apply()
        Log.d(TAG, "分支名称已更新: $branchName")
    }
    
    /**
     * 获取认证类型
     */
    fun getAuthType(): AuthType {
        val authTypeStr = sharedPreferences.getString(KEY_AUTH_TYPE, DEFAULT_AUTH_TYPE) ?: DEFAULT_AUTH_TYPE
        return try {
            AuthType.valueOf(authTypeStr)
        } catch (e: IllegalArgumentException) {
            AuthType.HTTPS
        }
    }
    
    /**
     * 设置认证类型
     */
    fun setAuthType(authType: AuthType) {
        sharedPreferences.edit()
            .putString(KEY_AUTH_TYPE, authType.name)
            .apply()
        Log.d(TAG, "认证类型已更新: ${authType.name}")
    }
    
    /**
     * 检查 Git 设置是否完整
     */
    fun isGitSetupComplete(): Boolean {
        val token = getGitHubToken()
        val repoUrl = getRepoUrl()
        
        return token.isNotEmpty() && repoUrl.isNotEmpty()
    }
    
    /**
     * 获取 Git 设置状态
     */
    fun getGitSetupStatus(): GitSetupStatus {
        val token = getGitHubToken()
        val repoUrl = getRepoUrl()
        val username = getGitHubUsername()
        
        val missingFields = mutableListOf<String>()
        
        if (token.isEmpty()) missingFields.add("GitHub Token")
        if (repoUrl.isEmpty()) missingFields.add("仓库 URL")
        if (username.isEmpty()) missingFields.add("GitHub 用户名")
        
        return GitSetupStatus(
            isComplete = missingFields.isEmpty(),
            missingFields = missingFields,
            tokenPresent = token.isNotEmpty(),
            repoUrlPresent = repoUrl.isNotEmpty(),
            usernamePresent = username.isNotEmpty()
        )
    }
    
    /**
     * 清除所有 Git 设置
     */
    fun clearAllSettings() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "所有 Git 设置已清除")
    }
    
    /**
     * 获取所有设置的摘要
     */
    fun getSettingsSummary(): String {
        return """
            Git 设置摘要:
            • GitHub 用户名: ${getGitHubUsername()}
            • 仓库 URL: ${getRepoUrl()}
            • 分支: ${getBranchName()}
            • 认证类型: ${getAuthType().name}
            • 自动提交: ${if (isAutoCommitEnabled()) "启用" else "禁用"}
            • 自动拉取: ${if (isAutoPullEnabled()) "启用" else "禁用"}
            • 冲突解决: ${getConflictResolution().name}
            • 设置完整性: ${if (isGitSetupComplete()) "完整" else "不完整"}
        """.trimIndent()
    }
}

/**
 * Git 设置状态
 */
data class GitSetupStatus(
    val isComplete: Boolean,
    val missingFields: List<String>,
    val tokenPresent: Boolean,
    val repoUrlPresent: Boolean,
    val usernamePresent: Boolean
) {
    /**
     * 获取格式化状态信息
     */
    fun getFormattedStatus(): String {
        return if (isComplete) {
            "Git 设置完整，可以正常使用"
        } else {
            "Git 设置不完整，缺少以下字段:\n${missingFields.joinToString("\n• ", "• ")}"
        }
    }
}