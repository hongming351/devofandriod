# GitHub Actions 自动部署配置指南

## 1. 创建 GitHub Actions 工作流文件

在你的 Hexo 博客仓库根目录下创建 `.github/workflows/deploy.yml` 文件：

```yaml
name: Deploy Hexo Blog

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
        github_token: ${{ secrets.GITHUB_TOKEN }}
        publish_dir: ./public
        publish_branch: gh-pages
        force_orphan: true
        user_name: 'github-actions[bot]'
        user_email: 'github-actions[bot]@users.noreply.github.com'
```

## 2. 必需的 Hexo 配置文件

确保你的 Hexo 博客仓库包含以下文件：

### `package.json` (示例)
```json
{
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
```

### `_config.yml` (关键配置部分)
```yaml
# Hexo Configuration
## Docs: https://hexo.io/docs/configuration.html
## Source: https://github.com/hexojs/hexo/

# Site
title: 你的博客标题
subtitle: ''
description: ''
keywords:
author: 你的名字
language: zh-CN
timezone: 'Asia/Shanghai'

# URL
## If your site is put in a subdirectory, set url as 'http://yoursite.com/child' and root as '/child/'
url: https://your-username.github.io
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
  repo: https://github.com/your-username/your-username.github.io.git
  branch: gh-pages
```

## 3. 仓库结构要求

你的 Hexo 博客仓库应该有以下结构：

```
your-username.github.io/
├── .github/
│   └── workflows/
│       └── deploy.yml          # GitHub Actions 工作流
├── source/
│   ├── _posts/                 # 博客文章目录
│   │   ├── 2025-03-25-文章标题.md
│   │   └── ...
│   ├── _drafts/                # 草稿目录（可选）
│   └── ...
├── themes/                     # Hexo 主题目录
│   └── landscape/              # 默认主题
├── scaffolds/                  # 模板目录
├── public/                     # 生成的静态文件（由 hexo generate 创建）
├── _config.yml                 # Hexo 配置文件
├── package.json                # Node.js 依赖配置
└── README.md                   # 仓库说明
```

## 4. 在 Android 应用中集成

### 创建 GitHub Actions 配置文件
在你的 Android 应用中，可以添加一个功能来生成 GitHub Actions 配置文件：

```kotlin
fun generateGitHubActionsConfig(): String {
    return """name: Deploy Hexo Blog

on:
  push:
    branches:
      - source

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout source branch
      uses: actions/checkout@v3
      with:
        ref: source
        fetch-depth: 0
    
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
"""
}
```

### 验证 GitHub Actions 配置
1. **推送测试**：从 Android 应用推送一篇文章到 `source` 分支
2. **检查 Actions**：在 GitHub 仓库的 Actions 标签页查看运行状态
3. **验证部署**：访问 `https://your-username.github.io` 查看更新

## 5. 常见问题解决

### 问题1：Actions 运行失败
**解决方案**：
1. 检查 `package.json` 中的依赖是否正确
2. 确保 `_config.yml` 中的配置正确
3. 查看 Actions 日志中的具体错误信息

### 问题2：静态文件未生成
**解决方案**：
1. 确保 Hexo 主题已正确安装
2. 检查 `source/_posts/` 目录下是否有 `.md` 文件
3. 验证 Node.js 和 Hexo 版本兼容性

### 问题3：部署到错误的分支
**解决方案**：
1. 确保工作流中配置了正确的分支名
2. 检查 `_config.yml` 中的 `deploy.branch` 设置
3. 验证 GitHub Token 权限

## 6. 优化建议

### 缓存优化
```yaml
- name: Cache node modules
  uses: actions/cache@v3
  with:
    path: node_modules
    key: ${{ runner.os }}-node-${{ hashFiles('package-lock.json') }}
    restore-keys: |
      ${{ runner.os }}-node-
```

### 多环境部署
```yaml
- name: Deploy to Production
  if: github.ref == 'refs/heads/source'
  uses: peaceiris/actions-gh-pages@v3
  with:
    github_token: ${{ secrets.GITHUB_TOKEN }}
    publish_dir: ./public
    publish_branch: gh-pages
```

### 通知功能
```yaml
- name: Send notification
  if: success()
  uses: rtCamp/action-slack-notify@v2
  env:
    SLACK_WEBHOOK: ${{ secrets.SLACK_WEBHOOK }}
    SLACK_MESSAGE: "Hexo 博客已成功部署！"
```

## 7. 完整工作流程

### 从 Android 应用到在线博客：
1. **编写文章**：在 Android 应用中使用 Markdown 编辑器
2. **保存文章**：文章保存到 `source/_posts/` 目录
3. **Git 提交**：自动或手动提交更改到 `source` 分支
4. **Git 推送**：推送到 GitHub 远程仓库
5. **Actions 触发**：GitHub Actions 自动运行
6. **构建部署**：生成静态文件并部署到 `gh-pages` 分支
7. **访问博客**：通过 `https://your-username.github.io` 访问

### 自动化程度：
- **全自动**：文章保存 → 自动提交 → 自动推送 → 自动部署
- **半自动**：文章保存 → 手动确认 → 一键部署
- **手动**：文章保存 → 在 Git 管理界面手动操作

**现在你的 Hexo 博客已经准备好通过 GitHub Actions 自动部署了！**