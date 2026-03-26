package com.example.hexobloguploader.config

import android.content.Context
import android.util.Log
import com.example.hexobloguploader.storage.BlogStorageManager
import java.io.File

/**
 * GitHub Actions 配置生成器
 * 用于生成 Hexo 博客自动部署的配置文件
 */
class GitHubActionsConfigGenerator(private val context: Context) {
    
    companion object {
        private const val TAG = "GitHubActionsConfig"
        
        // GitHub Actions 工作流配置模板
        private const val WORKFLOW_TEMPLATE = """name: Deploy Hexo Blog

on:
  push:
    branches:
      - source  # 监听 source 分支的推送

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout source branch
      uses: actions/checkout@v3
      with:
        ref: source
        fetch-depth: 0  # 获取所有历史记录，用于 hexo 生成
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
        cache: 'npm'
    
    - name: Install dependencies
      run: |
        npm install hexo-cli -g
        npm install
    
    - name: Generate static files
      run: |
        hexo clean
        hexo generate
    
    - name: Deploy to GitHub Pages
      uses: peaceiris/actions-gh-pages@v3
      with:
        github_token: $${{ secrets.GITHUB_TOKEN }}
        publish_dir: ./public
        publish_branch: gh-pages
        force_orphan: true
        user_name: 'github-actions[bot]'
        user_email: 'github-actions[bot]@users.noreply.github.com'
"""
        
        // package.json 模板
        private const val PACKAGE_JSON_TEMPLATE = """{
  "name": "hexo-site",
  "version": "1.0.0",
  "private": true,
  "hexo": {
    "version": "6.3.0"
  },
  "dependencies": {
    "hexo": "^6.3.0",
    "hexo-generator-archive": "^1.0.0",
    "hexo-generator-category": "^1.0.0",
    "hexo-generator-index": "^2.0.0",
    "hexo-generator-tag": "^1.0.0",
    "hexo-renderer-ejs": "^2.0.0",
    "hexo-renderer-marked": "^5.0.0",
    "hexo-renderer-stylus": "^2.0.0",
    "hexo-server": "^3.0.0",
    "hexo-deployer-git": "^3.0.0"
  },
  "scripts": {
    "build": "hexo generate",
    "clean": "hexo clean",
    "deploy": "hexo deploy",
    "server": "hexo server"
  }
}
"""
        
        // _config.yml 模板
        private const val CONFIG_YML_TEMPLATE = """# Hexo Configuration
## Docs: https://hexo.io/docs/configuration.html
## Source: https://github.com/hexojs/hexo/

# Site
title: %s
subtitle: ''
description: ''
keywords:
author: %s
language: zh-CN
timezone: 'Asia/Shanghai'

# URL
## If your site is put in a subdirectory, set url as 'http://yoursite.com/child' and root as '/child/'
url: %s
root: /
permalink: :year/:month/:day/:title/
permalink_defaults:
pretty_urls:
  trailing_index: true
  trailing_html: true

# Directory
source_dir: source
public_dir: public
tag_dir: tags
archive_dir: archives
category_dir: categories
code_dir: downloads/code
i18n_dir: :lang
skip_render:

# Writing
new_post_name: :title.md
default_layout: post
titlecase: false
external_link:
  enable: true
  field: site
  exclude: ''
filename_case: 0
render_drafts: false
post_asset_folder: false
relative_link: false
future: true
highlight:
  enable: true
  line_number: true
  auto_detect: false
  tab_replace: ''
  wrap: true
  hljs: false

# Deployment
## Docs: https://hexo.io/docs/one-command-deployment
deploy:
  type: git
  repo: %s
  branch: gh-pages
"""
    }
    
    private val storageManager = BlogStorageManager(context)
    
    /**
     * 生成 GitHub Actions 配置文件
     * @param blogTitle 博客标题
     * @param authorName 作者名称
     * @param githubUsername GitHub 用户名
     * @param repoName 仓库名称
     * @return 生成结果
     */
    fun generateConfigFiles(
        blogTitle: String = "我的 Hexo 博客",
        authorName: String = "Hexo 博客作者",
        githubUsername: String = "your-username",
        repoName: String = "your-username.github.io"
    ): ConfigGenerationResult {
        return try {
            // 初始化存储
            if (!storageManager.initStorage()) {
                return ConfigGenerationResult.error("存储初始化失败")
            }
            
            val blogRootDir = storageManager.blogRootDir
            
            // 创建 .github/workflows 目录
            val workflowsDir = File(blogRootDir, ".github/workflows")
            if (!workflowsDir.exists() && !workflowsDir.mkdirs()) {
                return ConfigGenerationResult.error("无法创建工作流目录")
            }
            
            // 生成 GitHub Actions 工作流文件
            val workflowFile = File(workflowsDir, "deploy.yml")
            if (!workflowFile.exists()) {
                workflowFile.writeText(WORKFLOW_TEMPLATE)
                Log.d(TAG, "GitHub Actions 工作流文件已创建: ${workflowFile.absolutePath}")
            }
            
            // 生成 package.json 文件
            val packageJsonFile = File(blogRootDir, "package.json")
            if (!packageJsonFile.exists()) {
                packageJsonFile.writeText(PACKAGE_JSON_TEMPLATE)
                Log.d(TAG, "package.json 文件已创建: ${packageJsonFile.absolutePath}")
            }
            
            // 生成 _config.yml 文件
            val configYmlFile = File(blogRootDir, "_config.yml")
            if (!configYmlFile.exists()) {
                val repoUrl = "https://github.com/$githubUsername/$repoName.git"
                val siteUrl = "https://$githubUsername.github.io"
                
                val configContent = CONFIG_YML_TEMPLATE.format(
                    blogTitle,
                    authorName,
                    siteUrl,
                    repoUrl
                )
                
                configYmlFile.writeText(configContent)
                Log.d(TAG, "_config.yml 文件已创建: ${configYmlFile.absolutePath}")
            }
            
            // 创建必要的目录结构
            createDirectoryStructure(blogRootDir)
            
            ConfigGenerationResult.success(
                message = "GitHub Actions 配置文件生成成功",
                workflowPath = workflowFile.absolutePath,
                packageJsonPath = packageJsonFile.absolutePath,
                configYmlPath = configYmlFile.absolutePath
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "生成配置文件失败: ${e.message}", e)
            ConfigGenerationResult.error("生成配置文件失败: ${e.message}")
        }
    }
    
    /**
     * 检查是否已配置 GitHub Actions
     */
    fun isGitHubActionsConfigured(): Boolean {
        return try {
            if (!storageManager.initStorage()) {
                return false
            }
            
            val blogRootDir = storageManager.blogRootDir
            val workflowFile = File(blogRootDir, ".github/workflows/deploy.yml")
            val packageJsonFile = File(blogRootDir, "package.json")
            val configYmlFile = File(blogRootDir, "_config.yml")
            
            workflowFile.exists() && packageJsonFile.exists() && configYmlFile.exists()
            
        } catch (e: Exception) {
            Log.e(TAG, "检查配置失败: ${e.message}", e)
            false
        }
    }
    
    /**
     * 获取配置状态
     */
    fun getConfigStatus(): ConfigStatus {
        return try {
            if (!storageManager.initStorage()) {
                return ConfigStatus.error("存储未初始化")
            }
            
            val blogRootDir = storageManager.blogRootDir
            
            val workflowFile = File(blogRootDir, ".github/workflows/deploy.yml")
            val packageJsonFile = File(blogRootDir, "package.json")
            val configYmlFile = File(blogRootDir, "_config.yml")
            
            ConfigStatus(
                workflowConfigured = workflowFile.exists(),
                packageJsonConfigured = packageJsonFile.exists(),
                configYmlConfigured = configYmlFile.exists(),
                workflowPath = workflowFile.absolutePath,
                packageJsonPath = packageJsonFile.absolutePath,
                configYmlPath = configYmlFile.absolutePath
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "获取配置状态失败: ${e.message}", e)
            ConfigStatus.error("获取配置状态失败: ${e.message}")
        }
    }
    
    /**
     * 创建必要的目录结构
     */
    private fun createDirectoryStructure(blogRootDir: File) {
        val directories = listOf(
            "source/_posts",
            "source/_drafts",
            "themes",
            "scaffolds",
            "public"
        )
        
        directories.forEach { dirPath ->
            val dir = File(blogRootDir, dirPath)
            if (!dir.exists()) {
                dir.mkdirs()
                Log.d(TAG, "创建目录: ${dir.absolutePath}")
            }
        }
        
        // 创建 README.md 文件
        val readmeFile = File(blogRootDir, "README.md")
        if (!readmeFile.exists()) {
            val readmeContent = """# Hexo 博客仓库

这是一个通过 Android 应用管理的 Hexo 博客仓库。

## 功能特点

1. **移动端编辑**：通过 Android 应用随时随地编写博客
2. **自动部署**：GitHub Actions 自动构建和部署
3. **Markdown 支持**：完整的 Markdown 编辑器
4. **Git 集成**：版本控制和同步

## 使用方法

1. 在 Android 应用中编写文章
2. 保存文章到本地
3. 提交并推送到 GitHub
4. GitHub Actions 自动部署到 GitHub Pages

## 目录结构

- `source/_posts/` - 博客文章
- `.github/workflows/` - GitHub Actions 工作流
- `themes/` - Hexo 主题
- `public/` - 生成的静态文件

## 注意事项

- 确保 GitHub 仓库已启用 GitHub Pages
- 首次使用需要配置 GitHub Token
- 建议定期备份重要文章

**祝你写作愉快！**"""
            
            readmeFile.writeText(readmeContent)
            Log.d(TAG, "README.md 文件已创建: ${readmeFile.absolutePath}")
        }
    }
    
    /**
     * 获取 GitHub Actions 工作流内容
     */
    fun getWorkflowContent(): String {
        return WORKFLOW_TEMPLATE
    }
    
    /**
     * 获取 package.json 内容
     */
    fun getPackageJsonContent(): String {
        return PACKAGE_JSON_TEMPLATE
    }
    
    /**
     * 获取 _config.yml 内容模板
     */
    fun getConfigYmlTemplate(): String {
        return CONFIG_YML_TEMPLATE
    }
}

/**
 * 配置生成结果
 */
data class ConfigGenerationResult(
    val success: Boolean,
    val message: String,
    val workflowPath: String? = null,
    val packageJsonPath: String? = null,
    val configYmlPath: String? = null,
    val error: String? = null
) {
    companion object {
        fun success(
            message: String,
            workflowPath: String? = null,
            packageJsonPath: String? = null,
            configYmlPath: String? = null
        ): ConfigGenerationResult {
            return ConfigGenerationResult(
                success = true,
                message = message,
                workflowPath = workflowPath,
                packageJsonPath = packageJsonPath,
                configYmlPath = configYmlPath
            )
        }
        
        fun error(message: String, error: String? = null): ConfigGenerationResult {
            return ConfigGenerationResult(
                success = false,
                message = message,
                error = error
            )
        }
    }
}

/**
 * 配置状态
 */
data class ConfigStatus(
    val workflowConfigured: Boolean = false,
    val packageJsonConfigured: Boolean = false,
    val configYmlConfigured: Boolean = false,
    val workflowPath: String? = null,
    val packageJsonPath: String? = null,
    val configYmlPath: String? = null,
    val error: String? = null
) {
    val isFullyConfigured: Boolean
        get() = workflowConfigured && packageJsonConfigured && configYmlConfigured
    
    fun getFormattedStatus(): String {
        return if (error != null) {
            "错误: $error"
        } else {
            """GitHub Actions 配置状态:
            
            工作流文件: ${if (workflowConfigured) "✓ 已配置" else "✗ 未配置"}
            package.json: ${if (packageJsonConfigured) "✓ 已配置" else "✗ 未配置"}
            _config.yml: ${if (configYmlConfigured) "✓ 已配置" else "✗ 未配置"}
            
            ${if (isFullyConfigured) "✅ 所有配置已完成，可以自动部署！" else "⚠️ 需要配置缺失的文件"}
            """
        }
    }
    
    companion object {
        fun error(message: String): ConfigStatus {
            return ConfigStatus(error = message)
        }
    }
}