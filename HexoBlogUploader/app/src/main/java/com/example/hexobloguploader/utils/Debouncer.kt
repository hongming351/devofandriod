package com.example.hexobloguploader.utils

import android.os.Handler
import android.os.Looper

/**
 * 防抖工具类
 * 用于防止频繁触发事件，例如实时搜索、预览更新等
 */
class Debouncer(private val delayMillis: Long) {
    
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null
    
    /**
     * 执行防抖操作
     * @param action 要执行的操作
     */
    fun debounce(action: () -> Unit) {
        // 取消之前的任务
        runnable?.let { handler.removeCallbacks(it) }
        
        // 创建新的任务
        runnable = Runnable {
            action()
            runnable = null
        }
        
        // 延迟执行
        handler.postDelayed(runnable!!, delayMillis)
    }
    
    /**
     * 取消待执行的任务
     */
    fun cancel() {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = null
    }
    
    /**
     * 检查是否有待执行的任务
     */
    fun hasPendingTask(): Boolean {
        return runnable != null
    }
    
    companion object {
        /**
         * 创建防抖器实例
         */
        fun create(delayMillis: Long = 500L): Debouncer {
            return Debouncer(delayMillis)
        }
    }
}