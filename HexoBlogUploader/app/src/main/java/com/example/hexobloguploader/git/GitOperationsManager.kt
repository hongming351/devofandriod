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
     * @param repoUrl 仓库URL，如 https://github.com/username/repo.git
     * @param token GitHub Personal Access Token
     * @param progressMonitor 进度监视器（可选）
     * @return 克隆成功返回true，失败返回false
     */
    fun cloneRepository(
        repoUrl: String,
        token: String,
        progressMonitor: ProgressMonitor? = null
    ): GitResult {
        return try {
            Log.d(TAG, "开始克隆仓库: $repoUrl")
            
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
            
            // 执行克隆
            val git = Git.cloneRepository()
                .setURI(repoUrl)
                .setDirectory(localPath)
                .setBranch(DEFAULT_BRANCH)
                .setCredentialsProvider(UsernamePasswordCredentialsProvider(token, ""))
                .setProgressMonitor(progressMonitor)
                .call()
            
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
            
