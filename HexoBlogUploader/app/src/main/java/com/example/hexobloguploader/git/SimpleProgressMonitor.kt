package com.example.hexobloguploader.git

import android.util.Log
import org.eclipse.jgit.lib.ProgressMonitor

/**
 * 简单的进度监视器
 * 用于显示 Git 操作的进度
 */
class SimpleProgressMonitor(
    private val tag: String = "GitProgress",
    private val onProgressUpdate: ((String, Int) -> Unit)? = null
) : ProgressMonitor {
    
    companion object {
        private const val TAG = "SimpleProgressMonitor"
    }
    
    private var totalTasks = 0
    private var currentTask = 0
    private var currentTaskTitle = ""
    private var currentTaskTotal = 0
    private var currentTaskProgress = 0
    
    override fun start(totalTasks: Int) {
        this.totalTasks = totalTasks
        this.currentTask = 0
        Log.d(TAG, "开始 $totalTasks 个任务")
        onProgressUpdate?.invoke("开始 $totalTasks 个任务", 0)
    }
    
    override fun beginTask(title: String, totalWork: Int) {
        currentTask++
        currentTaskTitle = title
        currentTaskTotal = totalWork
        currentTaskProgress = 0
        
        val taskInfo = "任务 $currentTask/$totalTasks: $title"
        Log.d(TAG, taskInfo)
        onProgressUpdate?.invoke(taskInfo, 0)
    }
    
    override fun update(completed: Int) {
        currentTaskProgress += completed
        
        if (currentTaskTotal > 0) {
            val percent = (currentTaskProgress * 100 / currentTaskTotal).coerceIn(0, 100)
            val progressInfo = "$currentTaskTitle: $currentTaskProgress/$currentTaskTotal ($percent%)"
            
            if (currentTaskProgress % 100 == 0 || currentTaskProgress == currentTaskTotal) {
                Log.d(TAG, progressInfo)
            }
            
            // 计算总体进度
            val overallPercent = if (totalTasks > 0) {
                ((currentTask - 1) * 100 + percent) / totalTasks
            } else {
                percent
            }
            
            onProgressUpdate?.invoke(progressInfo, overallPercent)
        }
    }
    
    override fun endTask() {
        Log.d(TAG, "任务完成: $currentTaskTitle")
        onProgressUpdate?.invoke("任务完成: $currentTaskTitle", 
            if (totalTasks > 0) (currentTask * 100 / totalTasks) else 100)
    }
    
    override fun isCancelled(): Boolean {
        return false
    }
    
    override fun showDuration(enabled: Boolean) {
        // 这个方法在JGit中是可选的，我们不需要实现具体逻辑
        Log.d(TAG, "showDuration: $enabled")
    }
    
    /**
     * 获取当前进度信息
     */
    fun getProgressInfo(): ProgressInfo {
        return ProgressInfo(
            totalTasks = totalTasks,
            currentTask = currentTask,
            currentTaskTitle = currentTaskTitle,
            currentTaskTotal = currentTaskTotal,
            currentTaskProgress = currentTaskProgress,
            currentTaskPercent = if (currentTaskTotal > 0) {
                (currentTaskProgress * 100 / currentTaskTotal).coerceIn(0, 100)
            } else 0,
            overallPercent = if (totalTasks > 0 && currentTask > 0) {
                ((currentTask - 1) * 100 + (currentTaskProgress * 100 / currentTaskTotal.coerceAtLeast(1))) / totalTasks
            } else 0
        )
    }
}

/**
 * 进度信息
 */
data class ProgressInfo(
    val totalTasks: Int,
    val currentTask: Int,
    val currentTaskTitle: String,
    val currentTaskTotal: Int,
    val currentTaskProgress: Int,
    val currentTaskPercent: Int,
    val overallPercent: Int
) {
    /**
     * 获取格式化进度信息
     */
    fun getFormattedInfo(): String {
        return """
            总体进度: $overallPercent%
            任务: $currentTask/$totalTasks
            当前任务: $currentTaskTitle
            任务进度: $currentTaskProgress/$currentTaskTotal ($currentTaskPercent%)
        """.trimIndent()
    }
}