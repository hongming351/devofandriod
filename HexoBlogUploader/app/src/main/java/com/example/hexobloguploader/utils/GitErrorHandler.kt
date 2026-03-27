package com.example.hexobloguploader.utils

import android.content.Context
import android.util.Log
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.errors.NoRemoteRepositoryException
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Git 错误处理工具类
 * 用于处理 Git 操作中的各种异常，提供友好的错误提示
 */
object GitErrorHandler {
    
    private const val TAG = "GitErrorHandler"
    
    /**
     * 处理 Git 异常，返回友好的错误消息
     */
    fun handleGitException(context: Context, exception: Exception): String {
        return when (exception) {
            is TransportException -> handleTransportException(context, exception)
            is NoRemoteRepositoryException -> handleNoRemoteRepositoryException(context, exception)
            is GitAPIException -> handleGitAPIException(context, exception)
            is IOException -> handleIOException(context, exception)
            else -> handleGenericException(context, exception)
        }
    }
    
    /**
     * 处理网络传输异常
     */
    private fun handleTransportException(context: Context, exception: TransportException): String {
        Log.e(TAG, "Git 传输异常: ${exception.message}", exception)
        
        // 检查是否是网络连接问题
        val cause = exception.cause
        return when {
            cause is ConnectException -> "网络连接失败，请检查网络连接"
            cause is UnknownHostException -> "无法连接到远程服务器，请检查网络设置"
            exception.message?.contains("authentication") == true -> "认证失败，请检查 Git Token 是否正确"
            exception.message?.contains("not authorized") == true -> "权限不足，请检查 Git Token 是否有推送权限"
            exception.message?.contains("timeout") == true -> "连接超时，请稍后重试"
            else -> "Git 传输错误: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 处理远程仓库不存在异常
     */
    private fun handleNoRemoteRepositoryException(
        context: Context, 
        exception: NoRemoteRepositoryException
    ): String {
        Log.e(TAG, "远程仓库不存在: ${exception.message}", exception)
        return "远程仓库不存在或 URL 错误，请检查仓库配置"
    }
    
    /**
     * 处理 Git API 异常
     */
    private fun handleGitAPIException(context: Context, exception: GitAPIException): String {
        Log.e(TAG, "Git API 异常: ${exception.message}", exception)
        
        return when {
            exception.message?.contains("conflict") == true -> "存在冲突，请先拉取远程更新"
            exception.message?.contains("nothing to commit") == true -> "没有需要提交的更改"
            exception.message?.contains("not a git repository") == true -> "不是 Git 仓库，请先初始化"
            exception.message?.contains("branch") == true -> "分支操作失败，请检查分支名称"
            else -> "Git 操作失败: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 处理 IO 异常
     */
    private fun handleIOException(context: Context, exception: IOException): String {
        Log.e(TAG, "IO 异常: ${exception.message}", exception)
        
        return when {
            exception.message?.contains("permission denied") == true -> "文件权限不足，请检查存储权限"
            exception.message?.contains("no space") == true -> "存储空间不足，请清理空间"
            exception.message?.contains("read-only") == true -> "文件系统只读，无法写入"
            else -> "文件操作失败: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 处理通用异常
     */
    private fun handleGenericException(context: Context, exception: Exception): String {
        Log.e(TAG, "通用异常: ${exception.message}", exception)
        return "操作失败: ${exception.message ?: "未知错误"}"
    }
    
    /**
     * 检查是否需要拉取远程更新
     */
    fun needsPull(exception: Exception): Boolean {
        return when (exception) {
            is TransportException -> {
                exception.message?.contains("rejected") == true ||
                exception.message?.contains("non-fast-forward") == true
            }
            is GitAPIException -> {
                exception.message?.contains("conflict") == true ||
                exception.message?.contains("diverged") == true
            }
            else -> false
        }
    }
    
    /**
     * 检查是否是认证错误
     */
    fun isAuthenticationError(exception: Exception): Boolean {
        return when (exception) {
            is TransportException -> {
                exception.message?.contains("authentication") == true ||
                exception.message?.contains("not authorized") == true ||
                exception.message?.contains("403") == true ||
                exception.message?.contains("401") == true
            }
            else -> false
        }
    }
    
    /**
     * 检查是否是网络错误
     */
    fun isNetworkError(exception: Exception): Boolean {
        val cause = exception.cause
        return cause is ConnectException || 
               cause is UnknownHostException ||
               exception.message?.contains("timeout") == true ||
               exception.message?.contains("connection") == true
    }
    
    /**
     * 检查是否是仓库配置错误
     */
    fun isRepositoryConfigError(exception: Exception): Boolean {
        return when (exception) {
            is NoRemoteRepositoryException -> true
            is GitAPIException -> {
                exception.message?.contains("not a git repository") == true ||
                exception.message?.contains("no remote") == true
            }
            else -> false
        }
    }
    
    /**
     * 获取错误建议
     */
    fun getErrorSuggestion(exception: Exception): String {
        return when {
            needsPull(exception) -> "建议先拉取远程更新，解决冲突后再推送"
            isAuthenticationError(exception) -> "建议检查 Git Token 是否正确，是否有推送权限"
            isNetworkError(exception) -> "建议检查网络连接，稍后重试"
            isRepositoryConfigError(exception) -> "建议检查仓库配置，确保远程仓库 URL 正确"
            else -> "请稍后重试，或联系开发者获取帮助"
        }
    }
    
    /**
     * 获取完整的错误信息（包含建议）
     */
    fun getFullErrorMessage(context: Context, exception: Exception): String {
        val errorMessage = handleGitException(context, exception)
        val suggestion = getErrorSuggestion(exception)
        
        return "$errorMessage\n\n建议: $suggestion"
    }
    
    /**
     * 记录错误日志
     */
    fun logError(exception: Exception, operation: String) {
        Log.e(TAG, "Git 操作失败 [$operation]: ${exception.message}", exception)
        
        // 这里可以添加更详细的日志记录，比如保存到文件
        // 或者发送到错误报告服务
    }
    
    /**
     * 检查是否应该重试
     */
    fun shouldRetry(exception: Exception): Boolean {
        return isNetworkError(exception) || 
               exception.message?.contains("timeout") == true ||
               exception.message?.contains("temporary") == true
    }
    
    /**
     * 获取重试建议
     */
    fun getRetryMessage(exception: Exception): String {
        return if (shouldRetry(exception)) {
            "网络问题导致操作失败，建议稍后重试"
        } else {
            "操作失败，请检查配置后重试"
        }
    }
}