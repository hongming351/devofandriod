package com.example.hexobloguploader.git

import android.content.Context
import android.util.Log
import com.example.hexobloguploader.storage.BlogStorageManager
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.MergeCommand
import org.eclipse.jgit.api.MergeResult
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
        const val DEFAULT_BRANCH = "main" // 默认分支，Hexo 通常使用 "source" 或 "main"
        const val HEXO_SOURCE_BRANCH = "source" // Hexo 源码分支
        const val HEXO_MASTER_BRANCH = "master" // 旧版 Git 默认分支
    }
    
    private val storageManager = BlogStorageManager(context)
    
    /**
     * 克隆仓库到本地存储目录
     * @param repoUrl 仓库URL，如 https://github.com/username/repo.git 或 git@github.com:username/repo.git
     * @param authToken 认证令牌（对于 HTTPS 是 PAT，对于 SSH 是私钥路径）
     * @param authType 认证类型（HTTPS 或 SSH）
     * @param progressMonitor 进度监视器（可选）
     * @return 克隆结果
     */
    fun cloneRepository(
        repoUrl: String,
        authToken: String? = null,
        authType: AuthType = AuthType.HTTPS,
        progressMonitor: ProgressMonitor? = null
    ): GitResult {
        return cloneRepositoryWithBranch(repoUrl, DEFAULT_BRANCH, authToken, authType, progressMonitor)
    }
    
    /**
     * 克隆仓库到本地存储目录（指定分支）
     * @param repoUrl 仓库URL
     * @param branch 分支名称
     * @param authToken 认证令牌
     * @param authType 认证类型
     * @param progressMonitor 进度监视器（可选）
     * @return 克隆结果
     */
    fun cloneRepositoryWithBranch(
        repoUrl: String,
        branch: String,
        authToken: String? = null,
        authType: AuthType = AuthType.HTTPS,
        progressMonitor: ProgressMonitor? = null
    ): GitResult {
        return try {
            Log.d(TAG, "开始克隆仓库: $repoUrl (分支: $branch, 认证类型: $authType)")
            
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
                .setBranch(branch)
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
                    
                    // 初始化 SSH 认证管理器
                    val sshAuthManager = SshAuthManager(context)
                    sshAuthManager.initSshSessionFactory()
                    
                    // 检查是否有 SSH 密钥
                    if (!sshAuthManager.hasSshKey()) {
                        Log.w(TAG, "没有配置 SSH 密钥，SSH 克隆可能失败")
                    } else {
                        Log.d(TAG, "已配置 SSH 密钥，使用 SSH 协议克隆")
                    }
                }
            }
            
            // 执行克隆
            val git = cloneCommand.call()
            git.close()
            
            Log.d(TAG, "仓库克隆成功: ${localPath.absolutePath} (分支: $branch)")
            GitResult.success("仓库克隆成功 (分支: $branch)", localPath.absolutePath)
            
        } catch (e: GitAPIException) {
            Log.e(TAG, "Git API 异常: ${e.message}", e)
            // 尝试其他分支
            if (e.message?.contains("branch") == true || e.message?.contains("not found") == true) {
                Log.d(TAG, "尝试其他分支...")
                return tryOtherBranches(repoUrl, branch, authToken, authType, progressMonitor)
            }
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
     * 尝试其他分支
     */
    private fun tryOtherBranches(
        repoUrl: String,
        originalBranch: String,
        authToken: String?,
        authType: AuthType,
        progressMonitor: ProgressMonitor?
    ): GitResult {
        // 尝试常见分支
        val branchesToTry = listOf(
            HEXO_SOURCE_BRANCH,
            HEXO_MASTER_BRANCH,
            "main",
            "master",
            "gh-pages"
        ).filter { it != originalBranch }
        
        for (branch in branchesToTry) {
            try {
                Log.d(TAG, "尝试分支: $branch")
                val cloneCommand = Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(storageManager.blogRootDir)
                    .setBranch(branch)
                    .setProgressMonitor(progressMonitor)
                
                if (authToken != null && authToken.isNotEmpty() && authType == AuthType.HTTPS) {
                    cloneCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(authToken, ""))
                }
                
                val git = cloneCommand.call()
                git.close()
                
                Log.d(TAG, "使用分支 $branch 克隆成功")
                return GitResult.success("仓库克隆成功 (使用分支: $branch)", storageManager.blogRootDir.absolutePath)
            } catch (e: Exception) {
                Log.d(TAG, "分支 $branch 失败: ${e.message}")
                // 继续尝试下一个分支
            }
        }
        
        return GitResult.error("无法找到有效的分支。尝试过的分支: ${branchesToTry.joinToString()}")
    }
    
    /**
     * 拉取最新更新
     * @param token 认证令牌（可选）
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
            // 使用错误处理器获取友好的错误消息
            com.example.hexobloguploader.utils.GitErrorHandler.logError(e, "commitAndPush")
            val errorMessage = com.example.hexobloguploader.utils.GitErrorHandler.getFullErrorMessage(context, e)
            GitResult.error(errorMessage)
        } catch (e: IOException) {
            com.example.hexobloguploader.utils.GitErrorHandler.logError(e, "commitAndPush")
            val errorMessage = com.example.hexobloguploader.utils.GitErrorHandler.getFullErrorMessage(context, e)
            GitResult.error(errorMessage)
        } catch (e: Exception) {
            com.example.hexobloguploader.utils.GitErrorHandler.logError(e, "commitAndPush")
            val errorMessage = com.example.hexobloguploader.utils.GitErrorHandler.getFullErrorMessage(context, e)
            GitResult.error(errorMessage)
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
        
        if (status.addedFiles.isNotEmpty()) {
            message.append("新增: ${status.addedFiles.size} 篇文章\n")
        }
        if (status.modifiedFiles.isNotEmpty()) {
            message.append("修改: ${status.modifiedFiles.size} 篇文章\n")
        }
        if (status.missingFiles.isNotEmpty()) {
            message.append("删除: ${status.missingFiles.size} 篇文章\n")
        }
        if (status.untrackedFiles.isNotEmpty()) {
            message.append("新增文件: ${status.untrackedFiles.size} 个\n")
        }
        
        // 添加前几个文件作为示例
        val sampleFiles = mutableListOf<String>()
        sampleFiles.addAll(status.addedFiles.take(3))
        sampleFiles.addAll(status.modifiedFiles.take(3))
        sampleFiles.addAll(status.missingFiles.take(3))
        
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
    
    /**
     * 检查冲突
     * @return 冲突信息
     */
    fun checkConflicts(): ConflictInfo {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return ConflictInfo.error("仓库未初始化")
            
            val status = git.status().call()
            val conflictingFiles = status.conflicting
            
            git.close()
            
            if (conflictingFiles.isNotEmpty()) {
                ConflictInfo.withConflicts(conflictingFiles.toList())
            } else {
                ConflictInfo.noConflicts()
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "检查冲突失败: ${e.message}", e)
            ConflictInfo.error("检查冲突失败: ${e.message}")
        }
    }
    
    /**
     * 拉取更新并检查冲突（增强版）
     * @param token 认证令牌（可选）
     * @param progressMonitor 进度监视器（可选）
     * @return 拉取结果，包含冲突信息
     */
    fun pullUpdatesWithConflictCheck(
        token: String? = null,
        progressMonitor: ProgressMonitor? = null
    ): Pair<GitResult, ConflictInfo> {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return Pair(
                GitResult.error("仓库未初始化"),
                ConflictInfo.error("仓库未初始化")
            )
            
            val pullCommand: PullCommand = git.pull()
                .setRemoteBranchName(DEFAULT_BRANCH)
                .setProgressMonitor(progressMonitor)
            
            // 如果需要认证，添加凭证
            token?.let {
                pullCommand.setCredentialsProvider(UsernamePasswordCredentialsProvider(it, ""))
            }
            
            val pullResult: PullResult = pullCommand.call()
            
            // 检查冲突
            val mergeResult = pullResult.mergeResult
            val conflictInfo = if (mergeResult != null && mergeResult.conflicts != null && mergeResult.conflicts.isNotEmpty()) {
                val conflictFiles = mergeResult.conflicts.keys.toList()
                ConflictInfo.withConflicts(conflictFiles, mergeResult)
            } else {
                ConflictInfo.noConflicts()
            }
            
            git.close()
            
            if (pullResult.isSuccessful && !conflictInfo.hasConflicts) {
                Pair(GitResult.success("拉取更新成功", localPath.absolutePath), conflictInfo)
            } else if (conflictInfo.hasConflicts) {
                Pair(GitResult.error("拉取成功但存在冲突，需要解决"), conflictInfo)
            } else {
                Pair(GitResult.error("拉取更新失败"), conflictInfo)
            }
            
        } catch (e: GitAPIException) {
            Log.e(TAG, "Git API 异常: ${e.message}", e)
            val errorMessage = com.example.hexobloguploader.utils.GitErrorHandler.getFullErrorMessage(context, e)
            Pair(GitResult.error(errorMessage), ConflictInfo.error("Git操作异常"))
        } catch (e: IOException) {
            Log.e(TAG, "IO 异常: ${e.message}", e)
            Pair(GitResult.error("IO操作失败: ${e.message}"), ConflictInfo.error("IO操作异常"))
        } catch (e: Exception) {
            Log.e(TAG, "未知异常: ${e.message}", e)
            Pair(GitResult.error("拉取失败: ${e.message}"), ConflictInfo.error("未知异常"))
        }
    }
    
    /**
     * 解决冲突
     * @param filePath 文件路径
     * @param resolution 解决方式
     * @return 解决结果
     */
    fun resolveConflict(
        filePath: String,
        resolution: ConflictResolution
    ): ConflictResolutionResult {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return ConflictResolutionResult.error("仓库未初始化")
            
            when (resolution) {
                ConflictResolution.USE_LOCAL -> {
                    // 使用本地版本
                    git.checkout()
                        .setStartPoint("HEAD")
                        .addPath(filePath)
                        .call()
                }
                ConflictResolution.USE_REMOTE -> {
                    // 使用远程版本（MERGE_HEAD）
                    git.checkout()
                        .setStartPoint("MERGE_HEAD")
                        .addPath(filePath)
                        .call()
                }
                ConflictResolution.MANUAL_RESOLVE -> {
                    // 手动解决 - 不做任何操作，让用户手动编辑
                    // 这里只是标记文件已解决
                }
            }
            
            // 如果选择了本地或远程版本，将文件标记为已解决
            if (resolution != ConflictResolution.MANUAL_RESOLVE) {
                git.add()
                    .addFilepattern(filePath)
                    .call()
            }
            
            git.close()
            
            ConflictResolutionResult.success(
                "冲突解决成功: $filePath (使用${resolution.name})",
                listOf(filePath)
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "解决冲突失败: ${e.message}", e)
            ConflictResolutionResult.error("解决冲突失败: ${e.message}", listOf(filePath))
        }
    }
    
    /**
     * 批量解决冲突
     * @param filePaths 文件路径列表
     * @param resolution 解决方式
     * @return 解决结果
     */
    fun resolveConflicts(
        filePaths: List<String>,
        resolution: ConflictResolution
    ): ConflictResolutionResult {
        val resolvedFiles = mutableListOf<String>()
        val failedFiles = mutableListOf<String>()
        
        for (filePath in filePaths) {
            val result = resolveConflict(filePath, resolution)
            if (result.success) {
                resolvedFiles.add(filePath)
            } else {
                failedFiles.add(filePath)
            }
        }
        
        return if (failedFiles.isEmpty()) {
            ConflictResolutionResult.success(
                "成功解决 ${resolvedFiles.size} 个文件的冲突",
                resolvedFiles
            )
        } else {
            ConflictResolutionResult.error(
                "成功解决 ${resolvedFiles.size} 个文件，失败 ${failedFiles.size} 个文件",
                failedFiles
            )
        }
    }
    
    /**
     * 提交冲突解决
     * @param message 提交信息
     * @return 提交结果
     */
    fun commitConflictResolution(message: String = "解决冲突"): GitResult {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return GitResult.error("仓库未初始化")
            
            // 检查是否还有未解决的冲突
            val status = git.status().call()
            if (status.conflicting.isNotEmpty()) {
                git.close()
                return GitResult.error("还有未解决的冲突: ${status.conflicting.joinToString()}")
            }
            
            // 提交
            val commit = git.commit()
                .setMessage(message)
                .call()
            
            git.close()
            
            GitResult.success("冲突解决提交成功", commit.id.name)
            
        } catch (e: Exception) {
            Log.e(TAG, "提交冲突解决失败: ${e.message}", e)
            GitResult.error("提交冲突解决失败: ${e.message}")
        }
    }
    
    /**
     * 完整的冲突解决流程
     * @param token GitHub Token（用于推送）
     * @param resolution 解决方式（可选，如果为null则只检查不解决）
     * @return 完整的解决结果
     */
    fun handleConflicts(
        token: String? = null,
        resolution: ConflictResolution? = null
    ): GitResult {
        // 1. 检查冲突
        val conflictInfo = checkConflicts()
        
        if (conflictInfo.error != null) {
            return GitResult.error("检查冲突失败: ${conflictInfo.error}")
        }
        
        if (!conflictInfo.hasConflicts) {
            return GitResult.success("没有冲突需要解决")
        }
        
        // 2. 如果有指定解决方式，则解决冲突
        if (resolution != null) {
            val resolveResult = resolveConflicts(conflictInfo.conflictFiles, resolution)
            
            if (!resolveResult.success) {
                return GitResult.error("解决冲突失败: ${resolveResult.message}")
            }
            
            // 3. 提交冲突解决
            val commitResult = commitConflictResolution("解决冲突: ${conflictInfo.conflictFiles.size} 个文件")
            
            if (!commitResult.success) {
                return GitResult.error("提交冲突解决失败: ${commitResult.message}")
            }
            
            // 4. 如果需要，推送到远程
            if (token != null) {
                return commitAndPush("解决冲突并推送", token, false)
            }
            
            return GitResult.success("冲突解决完成: ${resolveResult.resolvedFiles.size} 个文件已解决")
        } else {
            // 只返回冲突信息，不解决
            return GitResult.error(
                "发现 ${conflictInfo.conflictFiles.size} 个冲突文件，需要解决",
                conflictInfo.getFormattedInfo()
            )
        }
    }
    
    /**
     * 检测 Hexo 分支结构
     * @return Hexo 分支信息
     */
    fun detectHexoBranchStructure(): HexoBranchInfo {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return HexoBranchInfo()
            
            // 获取当前分支
            val currentBranch = git.repository.branch
            
            // 检查 Hexo 目录结构
            val sourceDir = File(localPath, "source")
            val postsDir = File(sourceDir, "_posts")
            val themesDir = File(localPath, "themes")
            val configFile = File(localPath, "_config.yml")
            
            val hasHexoStructure = sourceDir.exists() && postsDir.exists() && configFile.exists()
            
            // 尝试检测其他分支
            val branchList = git.branchList().call()
            val branches = branchList.map { it.name.replace("refs/heads/", "") }
            
            // 检测常见的 Hexo 分支
            val sourceBranch = branches.find { 
                it == "source" || it == "main" || it == "master" 
            } ?: currentBranch
            
            val pagesBranch = branches.find { 
                it == "gh-pages" || it == "master" || it == "main" 
            }
            
            git.close()
            
            HexoBranchInfo(
                sourceBranch = sourceBranch,
                pagesBranch = pagesBranch,
                hasHexoStructure = hasHexoStructure,
                postsDir = if (postsDir.exists()) postsDir.absolutePath else null,
                themesDir = if (themesDir.exists()) themesDir.absolutePath else null,
                configFile = if (configFile.exists()) configFile.absolutePath else null
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "检测 Hexo 分支结构失败: ${e.message}", e)
            HexoBranchInfo()
        }
    }
    
    /**
     * 获取 Hexo 文章列表
     * @return 文章信息列表
     */
    fun getHexoPosts(): List<HexoPostInfo> {
        return try {
            val localPath = storageManager.blogRootDir
            val postsDir = File(localPath, "source/_posts")
            
            if (!postsDir.exists() || !postsDir.isDirectory) {
                return emptyList()
            }
            
            val posts = mutableListOf<HexoPostInfo>()
            val markdownFiles = postsDir.listFiles { file -> 
                file.isFile && (file.name.endsWith(".md") || file.name.endsWith(".markdown"))
            }
            
            markdownFiles?.forEach { file ->
                try {
                    val content = file.readText()
                    val lines = content.lines()
                    
                    // 解析 Front-matter
                    var title: String? = null
                    var date: String? = null
                    val categories = mutableListOf<String>()
                    val tags = mutableListOf<String>()
                    
                    if (lines.isNotEmpty() && lines[0] == "---") {
                        var i = 1
                        while (i < lines.size && lines[i] != "---") {
                            val line = lines[i]
                            when {
                                line.startsWith("title:") -> title = line.substringAfter(":").trim().trim('"', '\'')
                                line.startsWith("date:") -> date = line.substringAfter(":").trim()
                                line.startsWith("categories:") -> {
                                    val cats = line.substringAfter(":").trim()
                                    if (cats.isNotEmpty()) {
                                        categories.addAll(parseList(cats))
                                    }
                                }
                                line.startsWith("tags:") -> {
                                    val tagStr = line.substringAfter(":").trim()
                                    if (tagStr.isNotEmpty()) {
                                        tags.addAll(parseList(tagStr))
                                    }
                                }
                            }
                            i++
                        }
                    }
                    
                    posts.add(HexoPostInfo(
                        fileName = file.name,
                        filePath = file.absolutePath,
                        title = title,
                        date = date,
                        categories = categories,
                        tags = tags,
                        content = content,
                        lastModified = file.lastModified()
                    ))
                } catch (e: Exception) {
                    Log.w(TAG, "解析文章失败: ${file.name}, ${e.message}")
                }
            }
            
            posts.sortedByDescending { it.lastModified }
            
        } catch (e: Exception) {
            Log.e(TAG, "获取 Hexo 文章列表失败: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * 解析列表字符串（支持 YAML 列表格式）
     */
    private fun parseList(listStr: String): List<String> {
        return try {
            // 移除方括号和引号
            val cleanStr = listStr.trim()
                .removePrefix("[")
                .removeSuffix("]")
                .replace("\"", "")
                .replace("'", "")
            
            if (cleanStr.isEmpty()) {
                return emptyList()
            }
            
            // 按逗号分割并清理
            cleanStr.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.w(TAG, "解析列表失败: $listStr, ${e.message}")
            emptyList()
        }
    }
    
    /**
     * 创建新的 Hexo 文章
     * @param title 文章标题
     * @param content 文章内容
     * @param categories 分类列表
     * @param tags 标签列表
     * @return 创建结果
     */
    fun createHexoPost(
        title: String,
        content: String,
        categories: List<String> = emptyList(),
        tags: List<String> = emptyList()
    ): GitResult {
        return try {
            val localPath = storageManager.blogRootDir
            val postsDir = File(localPath, "source/_posts")
            
            // 确保文章目录存在
            if (!postsDir.exists()) {
                if (!postsDir.mkdirs()) {
                    return GitResult.error("无法创建文章目录")
                }
            }
            
            // 生成文件名（使用标题和当前时间）
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(java.util.Date())
            val safeTitle = title.replace("[^a-zA-Z0-9\\u4e00-\\u9fa5\\-\\s]".toRegex(), "")
                .replace("\\s+".toRegex(), "-")
                .lowercase()
            
            val fileName = "$date-$safeTitle.md"
            val filePath = File(postsDir, fileName)
            
            // 生成 Front-matter
            val frontMatter = buildString {
                appendLine("---")
                appendLine("title: \"$title\"")
                appendLine("date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}")
                
                if (categories.isNotEmpty()) {
                    appendLine("categories:")
                    categories.forEach { category ->
                        appendLine("  - $category")
                    }
                }
                
                if (tags.isNotEmpty()) {
                    appendLine("tags:")
                    tags.forEach { tag ->
                        appendLine("  - $tag")
                    }
                }
                appendLine("---")
                appendLine()
            }
            
            // 写入文件
            filePath.writeText(frontMatter + content)
            
            GitResult.success("文章创建成功", filePath.absolutePath, fileName)
            
        } catch (e: Exception) {
            Log.e(TAG, "创建 Hexo 文章失败: ${e.message}", e)
            GitResult.error("创建文章失败: ${e.message}")
        }
    }
    
    /**
     * 检查是否是有效的 Hexo 仓库
     */
    fun isValidHexoRepository(): Boolean {
        return detectHexoBranchStructure().isValidHexoRepository()
    }
    
    /**
     * 切换到 Hexo 源码分支
     * @return 切换结果
     */
    fun switchToHexoSourceBranch(): GitResult {
        return try {
            val localPath = storageManager.blogRootDir
            val git = openRepository(localPath) ?: return GitResult.error("仓库未初始化")
            
            val branchInfo = detectHexoBranchStructure()
            val sourceBranch = branchInfo.sourceBranch ?: return GitResult.error("未检测到源码分支")
            
            // 检查当前分支
            val currentBranch = git.repository.branch
            if (currentBranch == sourceBranch) {
                git.close()
                return GitResult.success("已经在源码分支: $sourceBranch")
            }
            
            // 切换到源码分支
            git.checkout()
                .setName(sourceBranch)
                .call()
            
            git.close()
            
            GitResult.success("切换到源码分支: $sourceBranch")
            
        } catch (e: Exception) {
            Log.e(TAG, "切换分支失败: ${e.message}", e)
            GitResult.error("切换分支失败: ${e.message}")
        }
    }
    
    /**
     * 验证 Hexo 仓库配置
     * @return 验证结果
     */
    fun validateHexoRepository(): GitResult {
        val branchInfo = detectHexoBranchStructure()
        
        if (!branchInfo.isValidHexoRepository()) {
            return GitResult.error(
                "不是有效的 Hexo 仓库",
                branchInfo.getFormattedInfo()
            )
        }
        
        // 检查文章目录
        val postsDir = File(branchInfo.postsDir ?: "")
        if (!postsDir.exists() || !postsDir.isDirectory) {
            return GitResult.error("文章目录不存在或不是目录: ${postsDir.absolutePath}")
        }
        
        // 检查配置文件
        val configFile = File(branchInfo.configFile ?: "")
        if (!configFile.exists() || !configFile.isFile) {
            return GitResult.error("配置文件不存在: ${configFile.absolutePath}")
        }
        
        return GitResult.success(
            "Hexo 仓库验证通过",
            branchInfo.getFormattedInfo()
        )
    }
    
    /**
     * 获取 Hexo 仓库统计信息
     */
    fun getHexoRepositoryStats(): Map<String, Any> {
        val branchInfo = detectHexoBranchStructure()
        val posts = getHexoPosts()
        
        return mapOf<String, Any>(
            "branchInfo" to (branchInfo.getFormattedInfo() ?: ""),
            "postCount" to posts.size,
            "latestPost" to (posts.firstOrNull()?.title ?: "无"),
            "categories" to posts.flatMap { it.categories }.distinct().size,
            "tags" to posts.flatMap { it.tags }.distinct().size,
            "isValid" to branchInfo.isValidHexoRepository()
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

/**
 * 冲突信息
 */
data class ConflictInfo(
    val hasConflicts: Boolean = false,
    val conflictFiles: List<String> = emptyList(),
    val mergeResult: MergeResult? = null,
    val error: String? = null
) {
    companion object {
        fun noConflicts(): ConflictInfo {
            return ConflictInfo(hasConflicts = false)
        }
        
        fun withConflicts(files: List<String>, mergeResult: MergeResult? = null): ConflictInfo {
            return ConflictInfo(hasConflicts = true, conflictFiles = files, mergeResult = mergeResult)
        }
        
        fun error(message: String): ConflictInfo {
            return ConflictInfo(error = message)
        }
    }
    
    /**
     * 获取格式化冲突信息
     */
    fun getFormattedInfo(): String {
        return if (error != null) {
            "冲突检查错误: $error"
        } else if (!hasConflicts) {
            "没有冲突"
        } else {
            val fileList = conflictFiles.joinToString("\n  - ", "冲突文件:\n  - ")
            "$fileList\n\n共 ${conflictFiles.size} 个文件存在冲突"
        }
    }
}

/**
 * 冲突解决选项
 */
enum class ConflictResolution {
    USE_LOCAL,      // 使用本地版本
    USE_REMOTE,     // 使用远程版本
    MANUAL_RESOLVE  // 手动解决
}

/**
 * 冲突解决结果
 */
data class ConflictResolutionResult(
    val success: Boolean,
    val message: String,
    val resolvedFiles: List<String> = emptyList(),
    val remainingConflicts: List<String> = emptyList()
) {
    companion object {
        fun success(message: String, resolvedFiles: List<String> = emptyList()): ConflictResolutionResult {
            return ConflictResolutionResult(true, message, resolvedFiles)
        }
        
        fun error(message: String, remainingConflicts: List<String> = emptyList()): ConflictResolutionResult {
            return ConflictResolutionResult(false, message, remainingConflicts = remainingConflicts)
        }
    }
}

/**
 * Hexo 分支信息
 */
data class HexoBranchInfo(
    val sourceBranch: String? = null,      // 源码分支（如 source, main）
    val pagesBranch: String? = null,       // 页面分支（如 gh-pages, master）
    val hasHexoStructure: Boolean = false, // 是否有 Hexo 目录结构
    val postsDir: String? = null,          // 文章目录路径
    val themesDir: String? = null,         // 主题目录路径
    val configFile: String? = null         // 配置文件路径
) {
    /**
     * 获取格式化信息
     */
    fun getFormattedInfo(): String {
        return """
            Hexo 分支结构:
            源码分支: ${sourceBranch ?: "未检测到"}
            页面分支: ${pagesBranch ?: "未检测到"}
            Hexo 结构: ${if (hasHexoStructure) "✅ 完整" else "❌ 不完整"}
            文章目录: ${postsDir ?: "未找到"}
            主题目录: ${themesDir ?: "未找到"}
            配置文件: ${configFile ?: "未找到"}
        """.trimIndent()
    }
    
    /**
     * 检查是否是有效的 Hexo 仓库
     */
    fun isValidHexoRepository(): Boolean {
        return sourceBranch != null && hasHexoStructure && postsDir != null
    }
}

/**
 * Hexo 文章信息
 */
data class HexoPostInfo(
    val fileName: String,
    val filePath: String,
    val title: String? = null,
    val date: String? = null,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val content: String? = null,
    val lastModified: Long = 0
) {
    /**
     * 获取格式化信息
     */
    fun getFormattedInfo(): String {
        return """
            文章: $fileName
            路径: $filePath
            标题: ${title ?: "无"}
            日期: ${date ?: "无"}
            分类: ${categories.joinToString(", ")}
            标签: ${tags.joinToString(", ")}
            修改时间: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastModified))}
        """.trimIndent()
    }
}
