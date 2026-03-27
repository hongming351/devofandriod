# Hexo Blog Uploader - Git 模块使用指南

## 概述

Git 模块是 Hexo Blog Uploader 应用的核心组件，负责处理与 GitHub 仓库的所有交互。本指南详细介绍了 Git 模块的功能、使用方法和最佳实践。

## 已完成的功能模块

### 1. GitOperationsManager (Git 操作管理器)

**位置**: `app/src/main/java/com/example/hexobloguploader/git/GitOperationsManager.kt`

**核心功能**:

- ✅ **仓库克隆**: 支持 HTTPS 和 SSH 协议，自动检测分支
- ✅ **更新拉取**: 获取远程最新更改，支持冲突检测
- ✅ **提交推送**: 智能提交信息生成，自动处理删除文件
- ✅ **冲突处理**: 完整的冲突检测、解决和提交流程
- ✅ **Hexo 支持**: 自动检测 Hexo 分支结构，验证仓库配置
- ✅ **状态检查**: 实时仓库状态监控，文件变更跟踪
- ✅ **文章管理**: Hexo 文章创建、列表获取、Front-matter 解析

**关键特性**:

- 智能分支检测（自动尝试 source、main、master 等分支）
- 完整的错误处理和用户友好的错误消息
- 支持自动拉取避免推送冲突
- Hexo 目录结构验证和文章管理

### 2. GitSettingsManager (Git 设置管理器)

**位置**: `app/src/main/java/com/example/hexobloguploader/git/GitSettingsManager.kt`

**功能**:

- ✅ **设置存储**: 安全存储 GitHub Token、仓库 URL、分支等配置
- ✅ **设置验证**: 检查设置完整性，提示缺失信息
- ✅ **设置管理**: 支持设置保存、读取、清理和验证
- ✅ **认证类型**: 支持 HTTPS (Token) 和 SSH (密钥) 两种认证方式

### 3. SshAuthManager (SSH 认证管理器)

**位置**: `app/src/main/java/com/example/hexobloguploader/git/SshAuthManager.kt`

**功能**:

- ✅ **SSH 密钥管理**: 生成、保存、导入 SSH 密钥对
- ✅ **密钥验证**: 验证 SSH 密钥有效性
- ✅ **会话管理**: 初始化 SSH 会话工厂
- ✅ **密钥信息**: 获取密钥详细信息和使用状态

### 4. GitUserExperienceHelper (Git 用户体验助手)

**位置**: `app/src/main/java/com/example/hexobloguploader/git/GitUserExperienceHelper.kt`

**功能**:

- ✅ **错误处理**: 将技术错误转换为用户友好的消息
- ✅ **操作建议**: 根据错误类型提供具体的解决建议
- ✅ **状态描述**: 生成操作状态的可读描述
- ✅ **进度反馈**: 提供操作进度描述
- ✅ **确认提示**: 生成操作确认对话框内容
- ✅ **注意事项**: 提供操作相关的注意事项

### 5. GitErrorHandler (Git 错误处理器)

**位置**: `app/src/main/java/com/example/hexobloguploader/utils/GitErrorHandler.kt`

**功能**:

- ✅ **错误分类**: 识别不同类型的 Git 错误
- ✅ **错误日志**: 记录详细的错误信息
- ✅ **错误转换**: 将异常转换为用户友好的消息

### 6. SimpleProgressMonitor (简单进度监视器)

**位置**: `app/src/main/java/com/example/hexobloguploader/git/SimpleProgressMonitor.kt`

**功能**:

- ✅ **进度跟踪**: 实时跟踪 Git 操作进度
- ✅ **UI 更新**: 将进度更新到用户界面
- ✅ **取消支持**: 支持用户取消长时间运行的操作

## 数据模型

### 1. GitResult

Git 操作结果封装，包含成功状态、消息、详细信息和附加数据。

### 2. GitStatus

仓库状态信息，包括分支、提交历史、文件变更等。

### 3. ConflictInfo

冲突信息，包含冲突文件列表和合并结果。

### 4. HexoBranchInfo

Hexo 分支结构信息，检测源码分支、页面分支和目录结构。

### 5. HexoPostInfo

Hexo 文章信息，包含标题、日期、分类、标签等 Front-matter 数据。

## 使用流程

### 1. 初始设置

```kotlin
val settingsManager = GitSettingsManager(context)
settingsManager.saveGitHubUsername("your-username")
settingsManager.saveRepoUrl("https://github.com/username/repo.git")
settingsManager.saveBranchName("source")
settingsManager.saveAuthToken("your_github_token")
settingsManager.saveAuthType(AuthType.HTTPS)
```

### 2. 克隆仓库

```kotlin
val gitManager = GitOperationsManager(context)
val result = gitManager.cloneRepository(
    repoUrl = "https://github.com/username/repo.git",
    authToken = "your_token",
    authType = AuthType.HTTPS
)

if (result.success) {
    // 克隆成功
} else {
    // 处理错误
    val errorMessage = GitUserExperienceHelper(context).getCompleteErrorMessageFromResult(result)
}
```

### 3. 编辑和提交文章

```kotlin
// 创建新文章
val createResult = gitManager.createHexoPost(
    title = "文章标题",
    content = "文章内容",
    categories = listOf("技术", "博客"),
    tags = listOf("Android", "Kotlin")
)

// 提交更改
val commitResult = gitManager.commitAndPush(
    message = "添加新文章: 文章标题",
    token = "your_token",
    autoPull = true
)
```

### 4. 处理冲突

```kotlin
// 检查冲突
val conflictInfo = gitManager.checkConflicts()

if (conflictInfo.hasConflicts) {
    // 显示冲突解决指南
    val guide = GitUserExperienceHelper(context).getConflictResolutionGuide(conflictInfo)
    
    // 解决冲突（使用本地版本）
    val resolveResult = gitManager.resolveConflicts(
        filePaths = conflictInfo.conflictFiles,
        resolution = ConflictResolution.USE_LOCAL
    )
    
    // 提交冲突解决
    gitManager.commitConflictResolution("解决冲突")
}
```

### 5. 获取文章列表

```kotlin
val posts = gitManager.getHexoPosts()
posts.forEach { post ->
    Log.d("Post", "标题: ${post.title}, 日期: ${post.date}")
}
```

## 最佳实践

### 1. 错误处理

```kotlin
try {
    val result = gitManager.pullUpdates(token = "your_token")
    if (!result.success) {
        val helper = GitUserExperienceHelper(context)
        val errorMessage = helper.getCompleteErrorMessageFromResult(result)
        // 显示错误消息给用户
    }
} catch (e: Exception) {
    val helper = GitUserExperienceHelper(context)
    val errorMessage = helper.getCompleteErrorMessage(e)
    // 显示友好的错误消息
}
```

### 2. 进度反馈

```kotlin
val progressMonitor = SimpleProgressMonitor { progress, total ->
    val description = GitUserExperienceHelper(context)
        .getProgressDescription("clone", progress, total)
    // 更新 UI 进度显示
}

gitManager.cloneRepository(
    repoUrl = "url",
    authToken = "token",
    progressMonitor = progressMonitor
)
```

### 3. 设置验证

```kotlin
val settingsManager = GitSettingsManager(context)
val setupStatus = settingsManager.getGitSetupStatus()

if (!setupStatus.isComplete) {
    // 提示用户完善设置
    val missingFields = setupStatus.missingFields.joinToString("\n")
    // 显示缺失的设置项
}
```

### 4. Hexo 仓库验证

```kotlin
val gitManager = GitOperationsManager(context)
val validationResult = gitManager.validateHexoRepository()

if (!validationResult.success) {
    // 显示验证失败信息
    Log.e("Validation", validationResult.message)
}
```

## 常见问题解决

### 1. 认证失败

**问题**: `Authentication failed` 或 `not authorized`
**解决**:

- 检查 GitHub Token 是否正确
- 确保 Token 有仓库访问权限
- 尝试重新生成 Token

### 2. 仓库不存在

**问题**: `Repository not found`
**解决**:

- 检查仓库 URL 是否正确
- 确保仓库是公开的或你有访问权限
- 检查仓库名称拼写

### 3. 分支不存在

**问题**: `branch not found`
**解决**:

- 检查分支名称是否正确
- 查看远程仓库有哪些分支
- 尝试使用默认分支（main、master、source）

### 4. 推送被拒绝

**问题**: `rejected` 或 `non-fast-forward`
**解决**:

- 先拉取远程更新
- 解决可能存在的冲突
- 重新推送

### 5. 存在冲突

**问题**: `conflict` 或 `merge conflict`
**解决**:

- 查看冲突文件列表
- 选择使用本地版本或远程版本
- 手动编辑冲突文件解决

## 测试验证

已创建完整的测试套件 `GitModuleTest.kt`，验证以下功能:

1. ✅ GitSettingsManager - 设置管理功能
2. ✅ GitOperationsManager - 基础操作功能
3. ✅ GitUserExperienceHelper - 用户体验功能
4. ✅ SshAuthManager - SSH 认证功能
5. ✅ 冲突处理功能
6. ✅ Hexo 仓库功能
7. ✅ GitResult 和 AuthType

## 集成建议

### 1. 与 EditPostActivity 集成

在文章编辑完成后自动提交:

```kotlin
// 在 EditPostActivity 中
private fun autoCommitIfEnabled() {
    val settingsManager = GitSettingsManager(this)
    if (settingsManager.isAutoCommitEnabled()) {
        val gitManager = GitOperationsManager(this)
        gitManager.smartCommitAndPush(settingsManager.getAuthToken())
    }
}
```

### 2. 与 MainActivity 集成

应用启动时检查仓库状态:

```kotlin
// 在 MainActivity 中
private fun checkRepositoryStatus() {
    val gitManager = GitOperationsManager(this)
    val status = gitManager.checkRepositoryStatus()
    
    if (!status.isInitialized) {
        // 提示用户克隆仓库
    } else if (status.hasChanges) {
        // 显示未提交的更改
    }
}
```

### 3. 与 SettingsActivity 集成

提供完整的 Git 设置界面:

```kotlin
// 在 SettingsActivity 中
private fun setupGitSettings() {
    val settingsManager = GitSettingsManager(this)
    val setupStatus = settingsManager.getGitSetupStatus()
    
    // 显示设置状态
    val helper = GitUserExperienceHelper(this)
    val statusText = helper.getGitSetupCheckResult(settingsManager)
}
```

## 性能优化

### 1. 异步操作

所有 Git 操作都应在后台线程执行:

```kotlin
viewModelScope.launch(Dispatchers.IO) {
    val result = gitManager.cloneRepository(repoUrl, token)
    withContext(Dispatchers.Main) {
        // 更新 UI
    }
}
```

### 2. 进度反馈

使用 `SimpleProgressMonitor` 提供实时进度反馈:

```kotlin
val progressMonitor = SimpleProgressMonitor { progress, total ->
    updateProgress(progress, total)
}
```

### 3. 错误恢复

实现错误恢复机制:

```kotlin
fun safeGitOperation(operation: () -> GitResult): GitResult {
    return try {
        operation()
    } catch (e: Exception) {
        GitResult.error("操作失败: ${e.message}")
    }
}
```

## 总结

Git 模块为 Hexo Blog Uploader 提供了完整的 Git 操作支持，包括:

1. **完整的 Git 工作流**: 克隆、拉取、提交、推送
2. **智能错误处理**: 用户友好的错误消息和解决建议
3. **Hexo 专门支持**: 自动检测 Hexo 分支结构，文章管理
4. **多认证支持**: HTTPS Token 和 SSH 密钥
5. **冲突处理**: 完整的冲突检测和解决流程
6. **用户体验优化**: 进度反馈、确认提示、操作指南

通过这个模块，用户可以轻松地在手机上管理 Hexo 博客，实现文章的编辑、提交和同步到 GitHub，触发自动部署流程。
