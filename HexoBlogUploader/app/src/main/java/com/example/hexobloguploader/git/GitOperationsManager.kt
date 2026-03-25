package com.example.hexobloguploader.git

import android.content.Context
import android.util.Log
import com.example.hexobloguploader.storage.BlogStorageManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.Status
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import java.io.File
import java.io.IOException

/**
 * Git 操作管理器
 * 负责克隆、更新、提交 Hexo 博客仓库
 */
class GitOperationsManager(private val context: Context) {
    
    companion object {
        private const val TAG = "GitOperationsManager"
        const val DEFAULT_BRANCH = "source" // Hexo 源码分支
    }
    
    private val storageManager = BlogStorageManager(context)
    
    /**
     * 克隆仓库到本地存储目录
     * @param repoUrl 仓库URL，如 https://github.com/username/repo.git 或 git@github.com:username/repo.git
     * @param authToken 认证令牌（对于 HTTPS 是 PAT，对于 SSH 是私钥路径）
     * @param authType 认证类型（HTTPS 或 SSH）
     * @param progressMonitor 进度监视器（可选）
     * @return 克隆成功返回true，失败返回false
     */
    fun cloneRepository(
        repoUrl: String,
        authToken: String? = null,
        authType: AuthType = AuthType.HTTPS,
        progressMonitor: ProgressMonitor? = null
    ): GitResult {
        return try {
            Log.d(TAG, "开始克隆仓库: $repoUrl (认证类型: $authType)")
            
            // 确保存储目录存在
            if (!storageManager.initStorage()) {
                return GitResult.error("存储目录初始化失败")
            }
            
            val localPath = storageManager.blogRootDir
            
            // 如果目录已存在且非空，先删除
            if (localPath.exists() && localPath.list()?.isNotEmpty() == true) {
                Log.d(TAG, "目标目录非空，尝试清理...")
                if (!deleteDirectory(localPath)) {
                    return GitResult.error("无法清理现有目录")
                }
            }
            
            // 创建目录
            if (!localPath.mkdirs()) {
                return GitResult.error("无法创建目录: ${localPath.absolutePath}")
            }
            
            // 构建克隆命令
            val cloneCommand = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setBranch(DEFAULT_BRANCH)
                .setProgressMonitor(progressMonitor)
            
            // 设置认证
            when (authType) {
                AuthType.HTTPS -> {
                    if (authToken != null && authToken.isNotEmpty()) {
                        cloneCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(authToken, ""))
                    }
                }
                AuthType.SSH -> {
                    // SSH 认证需要配置 SSH 会话工厂
                    // 这里简化处理，实际应用中需要配置 SSH 密钥
                    Log.d(TAG, "使用 SSH 协议克隆，需要配置 SSH 密钥")
                }
            }
            
            // 执行克隆
            val git = cloneCommand.call()
            git.close()
            
            Log.d(TAG, "仓库克隆成功: ${localPath.absolutePath}")
            GitResult.success("仓库克隆成功", localPath.absolutePath)
            
        } catch (e: GitAPIException) {
            Log.e(TAG, "Git API 异常: ${e.message}", e)
            GitResult.error("Git 操作失败: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO 异常: ${e.message}", e)
            GitResult.error("IO 操作失败: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "未知异常: ${e.message}", e)
            GitResult.error("克隆失败: ${e.message}")
        }
    }
    
    /**
     * 拉取最新更新
     * @param progressMonitor 进度监视器（可选）
     * @return 拉取结果
     */
    fun pullUpdates(
        token: String? = null,
        progressMonitor: ProgressMonitor? = null
    ): GitResult {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return GitResult.error("仓库未初始化")
            
            val pullCommand: PullCommand = git.pull()
                .setRemoteBranchName(DEFAULT_BRANCH)
                .setProgressMonitor(progressMonitor)
            
            // 如果需要认证，添加凭证
            token?.let {
                pullCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(it, ""))
            }
            
            val pullResult: PullResult = pullCommand.call()
            git.close()
            
            if (pullResult.isSuccessful) {
                Log.d(TAG, "拉取更新成功")
                GitResult.success("拉取更新成功", localPath.absolutePath)
            } else {
                Log.w(TAG, "拉取更新失败: ${pullResult.toString()}")
                GitResult.error("拉取更新失败")
            }
            
        } catch (e: GitAPIException) {
            Log.e(TAG, "Git API 异常: ${e.message}", e)
            GitResult.error("Git 操作失败: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO 异常: ${e.message}", e)
            GitResult.error("IO 操作失败: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "未知异常: ${e.message}", e)
            GitResult.error("拉取失败: ${e.message}")
        }
    }
    
    /**
     * 检查仓库状态
     * @return 仓库状态信息
     */
    fun checkRepositoryStatus(): GitStatus {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return GitStatus.notInitialized()
            
            val status: Status = git.status().call()
            val branch = git.repository.branch
            val lastCommit = git.log().setMaxCount(1).call().firstOrNull()
            
            git.close()
            
            GitStatus(
                isInitialized = true,
                hasChanges = status.hasUncommittedChanges(),
                branch = branch,
                lastCommitMessage = lastCommit?.shortMessage,
                lastCommitAuthor = lastCommit?.authorIdent?.name,
                lastCommitDate = lastCommit?.authorIdent?.`when`,
                addedFiles = status.added,
                changedFiles = status.changed,
                conflictingFiles = status.conflicting,
                missingFiles = status.missing,
                modifiedFiles = status.modified,
                untrackedFiles = status.untracked
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "检查仓库状态失败: ${e.message}", e)
            GitStatus.error(e.message ?: "未知错误")
        }
    }
    
    /**
     * 提交更改
     * @param message 提交信息
     * @param token GitHub Token（用于推送）
     * @param autoPull 是否在推送前自动拉取更新（避免冲突）
     * @return 提交结果
     */
    fun commitAndPush(
        message: String,
        token: String,
        autoPull: Boolean = true
    ): GitResult {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return GitResult.error("仓库未初始化")
            
            // 检查状态
            val status = git.status().call()
            
            if (!status.hasUncommittedChanges()) {
                git.close()
                return GitResult.error("没有需要提交的更改")
            }
            
            Log.d(TAG, "检测到更改文件:")
            Log.d(TAG, "  新增: ${status.added.size}")
            Log.d(TAG, "  修改: ${status.modified.size}")
            Log.d(TAG, "  删除: ${status.missing.size}")
            Log.d(TAG, "  未跟踪: ${status.untracked.size}")
            
            // 添加所有更改（包括删除的文件）
            // 使用 setUpdate(true) 来检测删除的文件
            git.add()
                .setUpdate(true)  // 检测删除的文件
                .addFilepattern(".")
                .call()
            
            // 添加未跟踪的文件
            if (status.untracked.isNotEmpty()) {
                git.add()
                    .addFilepattern(".")
                    .call()
            }
            
            // 提交
            val commit = git.commit()
                .setMessage(message)
                .call()
            
            Log.d(TAG, "提交成功: ${commit.id.name}")
            
            // 如果需要，先拉取更新
            if (autoPull) {
                try {
                    Log.d(TAG, "推送前拉取更新...")
                    git.pull()
                        .setCredentialsProvider(UsernamePasswordCredentialsProvider(token, ""))
                        .call()
                    Log.d(TAG, "拉取更新成功")
                } catch (e: Exception) {
                    Log.w(TAG, "拉取更新失败，继续推送: ${e.message}")
                }
            }
            
            // 推送到远程
            git.push()
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(token, ""))
                .call()
            
            git.close()
            
            GitResult.success("提交并推送成功", commit.id.name)
            
        } catch (e: GitAPIException) {
            Log.e(TAG, "Git API 异常: ${e.message}", e)
            GitResult.error("Git 操作失败: ${e.message}")
        } catch (e: IOException) {
            Log.e(TAG, "IO 异常: ${e.message}", e)
            GitResult.error("IO 操作失败: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "未知异常: ${e.message}", e)
            GitResult.error("提交失败: ${e.message}")
        }
    }
    
    /**
     * 智能提交（自动生成提交信息）
     * @param token GitHub Token
     * @return 提交结果
     */
    fun smartCommitAndPush(token: String): GitResult {
        val status = checkRepositoryStatus()
        
        if (!status.hasChanges) {
            return GitResult.error("没有需要提交的更改")
        }
        
        // 生成智能提交信息
        val commitMessage = generateCommitMessage(status)
        
        return commitAndPush(commitMessage, token, true)
    }
    
    /**
     * 生成智能提交信息
     */
    private fun generateCommitMessage(status: GitStatus): String {
        val changeCount = status.getChangeCount()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
        
        val message = StringBuilder("更新博客文章 - $date\n\n")
        
        if (status.added.isNotEmpty()) {
            message.append("新增: ${status.added.size} 篇文章\n")
        }
        if (status.modified.isNotEmpty()) {
            message.append("修改: ${status.modified.size} 篇文章\n")
        }
        if (status.missing.isNotEmpty()) {
            message.append("删除: ${status.missing.size} 篇文章\n")
        }
        if (status.untracked.isNotEmpty()) {
            message.append("新增文件: ${status.untracked.size} 个\n")
        }
        
        // 添加前几个文件作为示例
        val sampleFiles = mutableListOf<String>()
        sampleFiles.addAll(status.added.take(3))
        sampleFiles.addAll(status.modified.take(3))
        sampleFiles.addAll(status.missing.take(3))
        
        if (sampleFiles.isNotEmpty()) {
            message.append("\n涉及文件:\n")
            sampleFiles.take(5).forEach { file ->
                message.append("  - $file\n")
            }
            if (sampleFiles.size > 5) {
                message.append("  - ...等 ${sampleFiles.size - 5} 个文件\n")
            }
        }
        
        return message.toString()
    }
    
    /**
     * 重置更改（撤销未提交的修改）
     * @param resetType 重置类型（HARD, SOFT, MIXED）
     * @return 重置结果
     */
    fun resetChanges(resetType: ResetCommand.ResetType = ResetCommand.ResetType.HARD): GitResult {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return GitResult.error("仓库未初始化")
            
            git.reset()
                .setMode(resetType)
                .call()
            
            git.close()
            
            GitResult.success("重置成功", resetType.name)
            
        } catch (e: Exception) {
            Log.e(TAG, "重置失败: ${e.message}", e)
            GitResult.error("重置失败: ${e.message}")
        }
    }
    
    /**
     * 检查仓库是否已初始化
     */
    fun isRepositoryInitialized(): Boolean {
        return try {
            val localPath = storageManager.blogRootDir
            val gitDir = File(localPath, ".git")
            gitDir.exists() && gitDir.isDirectory
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取仓库信息
     */
    fun getRepositoryInfo(): RepositoryInfo {
        val localPath = storageManager.blogRootDir
        val gitDir = File(localPath, ".git")
        
        return RepositoryInfo(
            localPath = localPath.absolutePath,
            gitDirExists = gitDir.exists(),
            gitDirPath = gitDir.absolutePath,
            storageInfo = storageManager.getStorageInfo()
        )
    }
    
    /**
     * 打开 Git 仓库
     */
    private fun openRepository(localPath: File): Git? {
        return try {
            val repositoryBuilder = FileRepositoryBuilder()
            val repository: Repository = repositoryBuilder
                .setGitDir(File(localPath, ".git"))
                .readEnvironment()
                .findGitDir()
                .build()
            
            Git(repository)
        } catch (e: Exception) {
            Log.e(TAG, "打开仓库失败: ${e.message}", e)
            null
        }
    }
    
    /**
     * 删除目录（递归）
     */
    private fun deleteDirectory(directory: File): Boolean {
        if (directory.exists()) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        deleteDirectory(file)
                    } else {
                        file.delete()
                    }
                }
            }
        }
        return directory.delete()
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
    
    /**
     * 获取格式化状态信息
     */
    fun getFormattedStatus(): String {
        return if (error != null) {
            "错误: $error"
        } else if (!isInitialized) {
            "仓库未初始化"
        } else {
            """
            分支: $branch
            最后提交: ${lastCommitMessage ?: "无"}
            作者: ${lastCommitAuthor ?: "未知"}
            时间: ${lastCommitDate?.toString() ?: "未知"}
            
            更改文件: ${getChangeCount()} 个
            ${if (addedFiles.isNotEmpty()) "  新增: ${addedFiles.size}" else ""}
            ${if (changedFiles.isNotEmpty()) "  修改: ${changedFiles.size}" else ""}
            ${if (modifiedFiles.isNotEmpty()) "  变更: ${modifiedFiles.size}" else ""}
            ${if (untrackedFiles.isNotEmpty()) "  未跟踪: ${untrackedFiles.size}" else ""}
            """.trimIndent()
        }
    }
}

/**
 * 仓库信息
 */
data class RepositoryInfo(
    val localPath: String,
    val gitDirExists: Boolean,
    val gitDirPath: String,
    val storageInfo: com.example.hexobloguploader.storage.StorageInfo
)