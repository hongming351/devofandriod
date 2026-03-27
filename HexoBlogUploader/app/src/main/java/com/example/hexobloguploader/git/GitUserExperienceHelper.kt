package com.example.hexobloguploader.git

import android.content.Context
import android.util.Log

/**
 * Git 用户体验助手
 * 提供更好的用户交互和错误处理
 */
class GitUserExperienceHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "GitUserExperienceHelper"
        
        // 常见错误消息映射
        private val errorMessages = mapOf(
            "Authentication failed" to "认证失败，请检查 GitHub Token 是否正确",
            "not authorized" to "认证失败，请检查 GitHub Token 是否有仓库访问权限",
            "remote: Repository not found" to "仓库不存在，请检查仓库 URL 是否正确",
            "remote: Invalid username or password" to "用户名或密码错误，请检查 GitHub Token",
            "remote: Permission to" to "没有权限访问该仓库",
            "Could not read from remote repository" to "无法从远程仓库读取，请检查网络连接",
            "Connection timed out" to "连接超时，请检查网络连接",
            "SSL peer certificate" to "SSL 证书错误，请检查网络设置",
            "branch.*not found" to "分支不存在，请检查分支名称",
            "conflict" to "存在冲突，需要手动解决",
            "merge conflict" to "合并冲突，需要手动解决",
            "non-fast-forward" to "远程有更新，请先拉取更新",
            "rejected" to "推送被拒绝，请先拉取更新",
            "already exists" to "文件已存在",
            "not a git repository" to "不是 Git 仓库，请先克隆仓库",
            "no such file or directory" to "文件或目录不存在",
            "permission denied" to "权限被拒绝，请检查文件权限",
            "disk full" to "磁盘空间不足",
            "out of memory" to "内存不足",
            "timeout" to "操作超时，请重试"
        )
        
        // 操作建议映射
        private val suggestions = mapOf(
            "认证失败" to listOf(
                "检查 GitHub Token 是否正确",
                "确保 Token 有仓库访问权限",
                "尝试重新生成 Token"
            ),
            "仓库不存在" to listOf(
                "检查仓库 URL 是否正确",
                "确保仓库是公开的或你有访问权限",
                "检查仓库名称拼写"
            ),
            "网络连接问题" to listOf(
                "检查网络连接",
                "尝试切换网络",
                "稍后重试"
            ),
            "分支不存在" to listOf(
                "检查分支名称是否正确",
                "查看远程仓库有哪些分支",
                "尝试使用默认分支"
            ),
            "存在冲突" to listOf(
                "查看冲突文件列表",
                "选择使用本地版本或远程版本",
                "手动编辑冲突文件"
            ),
            "推送被拒绝" to listOf(
                "先拉取远程更新",
                "解决可能存在的冲突",
                "重新推送"
            ),
            "磁盘空间不足" to listOf(
                "清理设备存储空间",
                "删除不必要的文件",
                "扩展存储空间"
            )
        )
    }
    
    /**
     * 获取友好的错误消息
     */
    fun getFriendlyErrorMessage(error: Exception): String {
        val errorMessage = error.message ?: "未知错误"
        
        // 查找匹配的错误消息
        for ((key, friendlyMessage) in errorMessages) {
            if (errorMessage.contains(key, ignoreCase = true)) {
                return friendlyMessage
            }
        }
        
        // 如果没有匹配的，返回原始错误消息
        return "操作失败: $errorMessage"
    }
    
    /**
     * 获取操作建议
     */
    fun getSuggestions(error: Exception): List<String> {
        val errorMessage = error.message ?: ""
        
        // 根据错误类型提供建议
        return when {
            errorMessage.contains("Authentication", ignoreCase = true) ||
            errorMessage.contains("not authorized", ignoreCase = true) ||
            errorMessage.contains("Invalid username", ignoreCase = true) -> {
                suggestions["认证失败"] ?: emptyList()
            }
            errorMessage.contains("Repository not found", ignoreCase = true) ||
            errorMessage.contains("Permission to", ignoreCase = true) -> {
                suggestions["仓库不存在"] ?: emptyList()
            }
            errorMessage.contains("Could not read", ignoreCase = true) ||
            errorMessage.contains("Connection timed out", ignoreCase = true) ||
            errorMessage.contains("SSL", ignoreCase = true) -> {
                suggestions["网络连接问题"] ?: emptyList()
            }
            errorMessage.contains("branch.*not found", ignoreCase = true) -> {
                suggestions["分支不存在"] ?: emptyList()
            }
            errorMessage.contains("conflict", ignoreCase = true) ||
            errorMessage.contains("merge conflict", ignoreCase = true) -> {
                suggestions["存在冲突"] ?: emptyList()
            }
            errorMessage.contains("rejected", ignoreCase = true) ||
            errorMessage.contains("non-fast-forward", ignoreCase = true) -> {
                suggestions["推送被拒绝"] ?: emptyList()
            }
            errorMessage.contains("disk full", ignoreCase = true) -> {
                suggestions["磁盘空间不足"] ?: emptyList()
            }
            else -> emptyList()
        }
    }
    
    /**
     * 获取完整的错误信息（包括建议）
     */
    fun getCompleteErrorMessage(error: Exception): String {
        val friendlyMessage = getFriendlyErrorMessage(error)
        val suggestionList = getSuggestions(error)
        
        return buildString {
            append(friendlyMessage)
            
            if (suggestionList.isNotEmpty()) {
                append("\n\n建议:\n")
                suggestionList.forEachIndexed { index, suggestion ->
                    append("${index + 1}. $suggestion\n")
                }
            }
        }
    }
    
    /**
     * 获取 Git 操作状态描述
     */
    fun getOperationStatusDescription(operation: String, status: GitResult): String {
        return when (operation) {
            "clone" -> {
                if (status.success) {
                    "仓库克隆成功！\n\n位置: ${status.details}"
                } else {
                    "仓库克隆失败\n\n${getCompleteErrorMessageFromResult(status)}"
                }
            }
            "pull" -> {
                if (status.success) {
                    "拉取更新成功！\n\n已获取最新内容"
                } else {
                    "拉取更新失败\n\n${getCompleteErrorMessageFromResult(status)}"
                }
            }
            "commit" -> {
                if (status.success) {
                    "提交成功！\n\n提交ID: ${status.details}"
                } else {
                    "提交失败\n\n${getCompleteErrorMessageFromResult(status)}"
                }
            }
            "push" -> {
                if (status.success) {
                    "推送成功！\n\n已同步到远程仓库"
                } else {
                    "推送失败\n\n${getCompleteErrorMessageFromResult(status)}"
                }
            }
            "status" -> {
                if (status.success) {
                    "仓库状态正常\n\n${status.details}"
                } else {
                    "获取仓库状态失败\n\n${getCompleteErrorMessageFromResult(status)}"
                }
            }
            else -> {
                if (status.success) {
                    "操作成功\n\n${status.details}"
                } else {
                    "操作失败\n\n${getCompleteErrorMessageFromResult(status)}"
                }
            }
        }
    }
    
    /**
     * 从 GitResult 获取完整的错误信息
     */
    private fun getCompleteErrorMessageFromResult(result: GitResult): String {
        val errorMessage = result.message
        val details = result.details ?: ""
        
        return buildString {
            append(errorMessage)
            if (details.isNotEmpty()) {
                append("\n\n详细信息: $details")
            }
        }
    }
    
    /**
     * 获取 Git 设置检查结果
     */
    fun getGitSetupCheckResult(settingsManager: GitSettingsManager): String {
        val status = settingsManager.getGitSetupStatus()
        
        return buildString {
            append("Git 设置检查:\n\n")
            
            if (status.isComplete) {
                append("✅ 设置完整，可以正常使用\n\n")
                append("GitHub 用户名: ${settingsManager.getGitHubUsername()}\n")
                append("仓库 URL: ${settingsManager.getRepoUrl()}\n")
                append("分支: ${settingsManager.getBranchName()}\n")
                append("认证类型: ${settingsManager.getAuthType().name}\n")
                append("自动提交: ${if (settingsManager.isAutoCommitEnabled()) "启用" else "禁用"}\n")
                append("自动拉取: ${if (settingsManager.isAutoPullEnabled()) "启用" else "禁用"}\n")
            } else {
                append("❌ 设置不完整，需要完善以下设置:\n\n")
                status.missingFields.forEach { field ->
                    append("• $field\n")
                }
                append("\n请在设置中完善以上信息")
            }
        }
    }
    
    /**
     * 获取冲突解决指南
     */
    fun getConflictResolutionGuide(conflictInfo: ConflictInfo): String {
        return buildString {
            append("发现冲突需要解决\n\n")
            append("冲突文件 (${conflictInfo.conflictFiles.size} 个):\n")
            conflictInfo.conflictFiles.forEachIndexed { index, file ->
                append("${index + 1}. $file\n")
            }
            
            append("\n解决方式:\n")
            append("1. 使用本地版本 - 保留你的修改\n")
            append("2. 使用远程版本 - 使用远程的修改\n")
            append("3. 手动解决 - 编辑文件解决冲突\n")
            
            append("\n建议:\n")
            append("• 如果冲突较少，建议手动解决\n")
            append("• 如果冲突较多，建议使用本地或远程版本\n")
            append("• 解决后需要重新提交\n")
        }
    }
    
    /**
     * 获取 Hexo 仓库验证结果
     */
    fun getHexoRepositoryValidationResult(branchInfo: HexoBranchInfo): String {
        return buildString {
            append("Hexo 仓库验证:\n\n")
            
            if (branchInfo.isValidHexoRepository()) {
                append("✅ 有效的 Hexo 仓库\n\n")
                append("源码分支: ${branchInfo.sourceBranch}\n")
                append("文章目录: ${branchInfo.postsDir}\n")
                append("配置文件: ${branchInfo.configFile ?: "未找到"}\n")
                append("主题目录: ${branchInfo.themesDir ?: "未找到"}\n")
                
                if (branchInfo.pagesBranch != null) {
                    append("页面分支: ${branchInfo.pagesBranch}\n")
                }
            } else {
                append("❌ 不是有效的 Hexo 仓库\n\n")
                
                if (branchInfo.sourceBranch == null) {
                    append("• 未检测到源码分支\n")
                }
                if (!branchInfo.hasHexoStructure) {
                    append("• 缺少 Hexo 目录结构\n")
                }
                if (branchInfo.postsDir == null) {
                    append("• 文章目录不存在\n")
                }
                
                append("\n建议:\n")
                append("• 确保克隆的是 Hexo 博客仓库\n")
                append("• 检查仓库是否包含 source/_posts 目录\n")
                append("• 检查是否在正确的分支上\n")
            }
        }
    }
    
    /**
     * 获取 Git 操作进度描述
     */
    fun getProgressDescription(operation: String, progress: Int, total: Int): String {
        val percentage = if (total > 0) (progress * 100 / total) else 0
        
        return when (operation) {
            "clone" -> "正在克隆仓库... ($percentage%)"
            "pull" -> "正在拉取更新... ($percentage%)"
            "push" -> "正在推送更改... ($percentage%)"
            "commit" -> "正在提交更改... ($percentage%)"
            "checkout" -> "正在切换分支... ($percentage%)"
            else -> "正在处理... ($percentage%)"
        }
    }
    
    /**
     * 获取 Git 操作成功提示
     */
    fun getSuccessMessage(operation: String, details: String? = null): String {
        val baseMessage = when (operation) {
            "clone" -> "仓库克隆成功"
            "pull" -> "更新拉取成功"
            "push" -> "更改推送成功"
            "commit" -> "更改提交成功"
            "checkout" -> "分支切换成功"
            "reset" -> "重置成功"
            "conflict_resolve" -> "冲突解决成功"
            else -> "操作成功"
        }
        
        return if (details != null) {
            "$baseMessage: $details"
        } else {
            baseMessage
        }
    }
    
    /**
     * 获取 Git 操作确认提示
     */
    fun getConfirmationMessage(operation: String, details: String? = null): String {
        return when (operation) {
            "clone" -> "确定要克隆仓库吗？\n\n这将下载整个仓库到本地。"
            "pull" -> "确定要拉取更新吗？\n\n这将获取远程的最新更改。"
            "push" -> "确定要推送更改吗？\n\n这将把你的更改同步到远程仓库。"
            "commit" -> "确定要提交更改吗？\n\n这将保存你的更改到本地仓库。"
            "reset" -> "确定要重置更改吗？\n\n这将撤销所有未提交的更改。"
            "delete" -> "确定要删除仓库吗？\n\n这将删除本地仓库的所有文件。"
            else -> "确定要执行此操作吗？"
        } + (details?.let { "\n\n$it" } ?: "")
    }
    
    /**
     * 获取 Git 操作注意事项
     */
    fun getOperationNotes(operation: String): List<String> {
        return when (operation) {
            "clone" -> listOf(
                "克隆需要网络连接",
                "克隆时间取决于仓库大小",
                "克隆会覆盖本地同名目录"
            )
            "pull" -> listOf(
                "拉取前建议先提交本地更改",
                "拉取可能会产生冲突",
                "拉取需要网络连接"
            )
            "push" -> listOf(
                "推送前建议先拉取更新",
                "推送需要有效的 GitHub Token",
                "推送后 GitHub Actions 会自动部署"
            )
            "commit" -> listOf(
                "提交只保存到本地仓库",
                "提交后可以推送到远程",
                "提交信息应该清晰描述更改"
            )
            "conflict_resolve" -> listOf(
                "解决冲突后需要重新提交",
                "建议先备份冲突文件",
                "手动解决可以保留双方更改"
            )
            else -> emptyList()
        }
    }
}