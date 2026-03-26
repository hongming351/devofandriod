package com.example.hexobloguploader.git

import android.content.Context
import android.util.Log

/**
 * 简化的 Git 操作管理器
 * 用于避免应用崩溃，实际功能暂时不可用
 */
class GitOperationsManager(private val context: Context) {
    
    companion object {
        private const val TAG = "GitOperationsManager"
        const val DEFAULT_BRANCH = "main" // 默认分支
    }
    
    /**
     * 克隆仓库（简化版）
     */
    fun cloneRepository(
        repoUrl: String,
        authToken: String? = null,
        authType: AuthType = AuthType.HTTPS
    ): GitResult {
        Log.d(TAG, "GitOperationsManager.cloneRepository called (简化版)")
        return GitResult.success("Git功能暂时不可用，请等待后续更新", "简化版实现")
    }
    
    /**
     * 拉取更新（简化版）
     */
    fun pullUpdates(token: String? = null): GitResult {
        Log.d(TAG, "GitOperationsManager.pullUpdates called (简化版)")
        // 使用token参数避免警告
        if (token != null) {
            Log.d(TAG, "Token provided: ${token.take(5)}...")
        }
        return GitResult.success("Git功能暂时不可用，请等待后续更新", "简化版实现")
    }
    
    /**
     * 检查仓库状态（简化版）
     */
    fun checkRepositoryStatus(): GitStatus {
        Log.d(TAG, "GitOperationsManager.checkRepositoryStatus called (简化版)")
        return GitStatus.notInitialized()
    }
    
    /**
     * 提交并推送（简化版）
     */
    fun commitAndPush(
        message: String,
        token: String,
        autoPull: Boolean = true
    ): GitResult {
        Log.d(TAG, "GitOperationsManager.commitAndPush called (简化版)")
        return GitResult.success("Git功能暂时不可用，请等待后续更新", "简化版实现")
    }
    
    /**
     * 智能提交（简化版）
     */
    fun smartCommitAndPush(token: String): GitResult {
        Log.d(TAG, "GitOperationsManager.smartCommitAndPush called (简化版)")
        return GitResult.success("Git功能暂时不可用，请等待后续更新", "简化版实现")
    }
    
    /**
     * 重置更改（简化版）
     */
    fun resetChanges(resetType: ResetType = ResetType.HARD): GitResult {
        Log.d(TAG, "GitOperationsManager.resetChanges called (简化版)")
        return GitResult.success("Git功能暂时不可用，请等待后续更新", "简化版实现")
    }
    
    /**
     * 检查仓库是否已初始化（简化版）
     */
    fun isRepositoryInitialized(): Boolean {
        return false
    }
    
    /**
     * 获取仓库信息（简化版）
     */
    fun getRepositoryInfo(): RepositoryInfo {
        return RepositoryInfo(
            localPath = "/storage/emulated/0/HexoBlog",
            gitDirExists = false,
            gitDirPath = "/storage/emulated/0/HexoBlog/.git",
            storageInfo = StorageInfo(
                blogRootPath = "/storage/emulated/0/HexoBlog",
                postCount = 0,
                totalSize = 0
            )
        )
    }
}

/**
 * 认证类型
 */
enum class AuthType {
    HTTPS,  // 使用 HTTPS 协议和 Personal Access Token
    SSH     // 使用 SSH 协议和 SSH 密钥
}

/**
 * 重置类型
 */
enum class ResetType {
    HARD, SOFT, MIXED
}

/**
 * Git 操作结果
 */
data class GitResult(
    val success: Boolean,
    val message: String,
    val details: String? = null,
    val data: Any? = null
) {
    companion object {
        fun success(message: String, details: String? = null, data: Any? = null): GitResult {
            return GitResult(true, message, details, data)
        }
        
        fun error(message: String, details: String? = null): GitResult {
            return GitResult(false, message, details)
        }
    }
}

/**
 * 仓库状态信息
 */
data class GitStatus(
    val isInitialized: Boolean = false,
    val hasChanges: Boolean = false,
    val branch: String? = null,
    val lastCommitMessage: String? = null,
    val lastCommitAuthor: String? = null,
    val lastCommitDate: java.util.Date? = null,
    val addedFiles: Set<String> = emptySet(),
    val changedFiles: Set<String> = emptySet(),
    val conflictingFiles: Set<String> = emptySet(),
    val missingFiles: Set<String> = emptySet(),
    val modifiedFiles: Set<String> = emptySet(),
    val untrackedFiles: Set<String> = emptySet(),
    val error: String? = null
) {
    companion object {
        fun notInitialized(): GitStatus {
            return GitStatus(isInitialized = false, error = "仓库未初始化")
        }
        
        fun error(message: String): GitStatus {
            return GitStatus(isInitialized = false, error = message)
        }
    }
    
    /**
     * 获取更改文件数量
     */
    fun getChangeCount(): Int {
        return addedFiles.size + changedFiles.size + modifiedFiles.size + untrackedFiles.size
    }
}

/**
 * 仓库信息
 */
data class RepositoryInfo(
    val localPath: String,
    val gitDirExists: Boolean,
    val gitDirPath: String,
    val storageInfo: StorageInfo
)

/**
 * 存储信息（简化版）
 */
data class StorageInfo(
    val blogRootPath: String,
    val postCount: Int,
    val totalSize: Long
) {
    fun getFormattedSize(): String {
        return when {
            totalSize < 1024 -> "$totalSize B"
            totalSize < 1024 * 1024 -> "${totalSize / 1024} KB"
            else -> "${totalSize / (1024 * 1024)} MB"
        }
    }
}