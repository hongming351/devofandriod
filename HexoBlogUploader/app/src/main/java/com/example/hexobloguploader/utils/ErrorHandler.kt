package com.example.hexobloguploader.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import java.io.FileNotFoundException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.AccessControlException

/**
 * 通用错误处理工具类
 * 用于处理应用中的各种异常，提供友好的错误提示
 */
object ErrorHandler {
    
    private const val TAG = "ErrorHandler"
    
    /**
     * 处理通用异常，返回友好的错误消息
     */
    fun handleException(context: Context, exception: Exception): String {
        return when (exception) {
            is IOException -> handleIOException(context, exception)
            is SecurityException -> handleSecurityException(context, exception)
            is IllegalArgumentException -> handleIllegalArgumentException(context, exception)
            is NullPointerException -> handleNullPointerException(context, exception)
            else -> handleGenericException(context, exception)
        }
    }
    
    /**
     * 处理 IO 异常
     */
    private fun handleIOException(context: Context, exception: IOException): String {
        Log.e(TAG, "IO 异常: ${exception.message}", exception)
        
        return when {
            exception is FileNotFoundException -> "文件不存在或无法访问"
            exception.message?.contains("permission denied") == true -> "文件权限不足，请检查存储权限"
            exception.message?.contains("no space") == true -> "存储空间不足，请清理空间"
            exception.message?.contains("read-only") == true -> "文件系统只读，无法写入"
            exception.message?.contains("ENOENT") == true -> "文件或目录不存在"
            exception.message?.contains("EACCES") == true -> "访问被拒绝，请检查权限"
            else -> "文件操作失败: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 处理安全异常
     */
    private fun handleSecurityException(context: Context, exception: SecurityException): String {
        Log.e(TAG, "安全异常: ${exception.message}", exception)
        
        return when {
            exception.message?.contains("permission") == true -> "权限不足，请检查应用权限设置"
            exception.message?.contains("access") == true -> "访问被拒绝，请检查权限"
            exception.message?.contains("security") == true -> "安全限制，无法执行此操作"
            else -> "安全错误: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 处理非法参数异常
     */
    private fun handleIllegalArgumentException(context: Context, exception: IllegalArgumentException): String {
        Log.e(TAG, "非法参数异常: ${exception.message}", exception)
        
        return when {
            exception.message?.contains("empty") == true -> "参数不能为空"
            exception.message?.contains("invalid") == true -> "参数无效"
            exception.message?.contains("range") == true -> "参数超出有效范围"
            else -> "参数错误: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 处理空指针异常
     */
    private fun handleNullPointerException(context: Context, exception: NullPointerException): String {
        Log.e(TAG, "空指针异常: ${exception.message}", exception)
        
        // 空指针异常通常是编程错误，给用户一个通用提示
        return "应用内部错误，请重启应用或联系开发者"
    }
    
    /**
     * 处理通用异常
     */
    private fun handleGenericException(context: Context, exception: Exception): String {
        Log.e(TAG, "通用异常: ${exception.message}", exception)
        
        return when {
            exception is ConnectException -> "网络连接失败，请检查网络连接"
            exception is UnknownHostException -> "无法连接到服务器，请检查网络设置"
            exception is SocketTimeoutException -> "连接超时，请稍后重试"
            exception is AccessControlException -> "访问控制错误，请检查权限"
            exception.message?.contains("timeout") == true -> "操作超时，请稍后重试"
            exception.message?.contains("network") == true -> "网络错误，请检查网络连接"
            exception.message?.contains("connection") == true -> "连接错误，请检查网络设置"
            else -> "操作失败: ${exception.message ?: "未知错误"}"
        }
    }
    
    /**
     * 显示错误提示
     */
    fun showErrorToast(context: Context, exception: Exception) {
        val errorMessage = handleException(context, exception)
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示错误提示（自定义消息）
     */
    fun showErrorToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * 显示成功提示
     */
    fun showSuccessToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示信息提示
     */
    fun showInfoToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 记录错误日志
     */
    fun logError(exception: Exception, operation: String) {
        Log.e(TAG, "操作失败 [$operation]: ${exception.message}", exception)
        
        // 这里可以添加更详细的日志记录，比如保存到文件
        // 或者发送到错误报告服务
    }
    
    /**
     * 记录警告日志
     */
    fun logWarning(message: String, operation: String) {
        Log.w(TAG, "操作警告 [$operation]: $message")
    }
    
    /**
     * 记录信息日志
     */
    fun logInfo(message: String, operation: String) {
        Log.i(TAG, "操作信息 [$operation]: $message")
    }
    
    /**
     * 检查是否是网络错误
     */
    fun isNetworkError(exception: Exception): Boolean {
        return exception is ConnectException || 
               exception is UnknownHostException ||
               exception is SocketTimeoutException ||
               exception.message?.contains("timeout") == true ||
               exception.message?.contains("network") == true ||
               exception.message?.contains("connection") == true
    }
    
    /**
     * 检查是否是文件权限错误
     */
    fun isPermissionError(exception: Exception): Boolean {
        return exception is SecurityException ||
               exception.message?.contains("permission") == true ||
               exception.message?.contains("access") == true ||
               exception.message?.contains("EACCES") == true
    }
    
    /**
     * 检查是否是存储空间错误
     */
    fun isStorageError(exception: Exception): Boolean {
        return exception.message?.contains("no space") == true ||
               exception.message?.contains("ENOSPC") == true ||
               exception.message?.contains("disk full") == true
    }
    
    /**
     * 获取错误建议
     */
    fun getErrorSuggestion(exception: Exception): String {
        return when {
            isNetworkError(exception) -> "建议检查网络连接，稍后重试"
            isPermissionError(exception) -> "建议检查应用权限设置，确保有必要的权限"
            isStorageError(exception) -> "建议清理存储空间，释放磁盘空间"
            exception is FileNotFoundException -> "建议检查文件路径是否正确，文件是否存在"
            else -> "请稍后重试，或联系开发者获取帮助"
        }
    }
    
    /**
     * 获取完整的错误信息（包含建议）
     */
    fun getFullErrorMessage(context: Context, exception: Exception): String {
        val errorMessage = handleException(context, exception)
        val suggestion = getErrorSuggestion(exception)
        
        return "$errorMessage\n\n建议: $suggestion"
    }
    
    /**
     * 检查是否应该重试
     */
    fun shouldRetry(exception: Exception): Boolean {
        return isNetworkError(exception) || 
               exception.message?.contains("timeout") == true ||
               exception.message?.contains("temporary") == true ||
               exception.message?.contains("retry") == true
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
    
    /**
     * 安全执行操作（带错误处理）
     */
    fun <T> safeExecute(
        context: Context,
        operation: String,
        action: () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = { showErrorToast(context, it) }
    ) {
        try {
            val result = action()
            onSuccess(result)
        } catch (e: Exception) {
            logError(e, operation)
            val errorMessage = getFullErrorMessage(context, e)
            onError(errorMessage)
        }
    }
    
    /**
     * 安全执行异步操作（带错误处理）
     */
    fun <T> safeExecuteAsync(
        context: Context,
        operation: String,
        action: () -> T,
        onSuccess: (T) -> Unit = {},
        onError: (String) -> Unit = { showErrorToast(context, it) }
    ) {
        Thread {
            try {
                val result = action()
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onSuccess(result)
                }
            } catch (e: Exception) {
                logError(e, operation)
                val errorMessage = getFullErrorMessage(context, e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    onError(errorMessage)
                }
            }
        }.start()
    }
}