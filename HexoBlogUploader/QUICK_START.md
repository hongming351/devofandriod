# Hexo 博客上传器 - 快速开始指南

## 🚀 一键安装 Android Studio

我们已经为你准备了多种安装方式：

### 方式1：运行下载脚本（推荐）

```cmd
cd "d:\devofandriod\HexoBlogUploader"
direct_download.bat
```

然后选择选项 1 下载安装程序。

### 方式2：手动下载

1. 访问：<https://developer.android.com/studio>
2. 下载 Windows 版本
3. 运行安装程序

### 方式3：使用 PowerShell 脚本

```powershell
cd "d:\devofandriod\HexoBlogUploader"
.\download_android_studio.ps1
```

## 📱 项目已准备就绪

你的 Hexo 博客上传器项目已经包含：

### ✅ 已完成配置

1. **项目结构**：完整的 Android 项目
2. **依赖配置**：
   - JGit 6.7.0 - Git 操作
   - Markwon 4.6.2 - Markdown 编辑
   - FilePicker 1.1.1 - 文件选择
3. **权限设置**：网络 + 存储权限
4. **基础代码**：MainActivity + 界面

### ✅ 可用文件

- `direct_download.bat` - 直接下载工具
- `install_android_studio.bat` - 安装指导
- `download_android_studio.ps1` - PowerShell 下载脚本
- `INSTALL_GUIDE.md` - 详细安装指南
- `README.md` - 完整项目文档

## 🛠️ 安装后步骤

### 第1步：安装 Android Studio

运行上述任一脚本完成安装。

### 第2步：打开项目

1. 启动 Android Studio
2. 选择 "Open"
3. 导航到：`d:\devofandriod\HexoBlogUploader`
4. 点击 "OK"

### 第3步：等待同步

Android Studio 会自动：

- 下载 Gradle 依赖
- 同步项目配置
- 构建项目

### 第4步：运行应用

1. 连接 Android 设备或启动模拟器
2. 点击 "Run" 按钮（绿色三角形）
3. 选择目标设备
4. 应用将运行在设备上

## 🔧 开始开发

### 核心功能待实现

1. **博客管理界面** - 显示博客列表
2. **Markdown 编辑器** - 编辑博客内容
3. **Git 操作** - 提交和推送博客
4. **文件上传** - 上传图片等资源

### 项目结构

```
app/src/main/java/com/example/hexobloguploader/
├── MainActivity.kt          # 主界面
├── BlogManager.kt          # 博客管理（待创建）
├── GitOperations.kt        # Git 操作（待创建）
└── MarkdownEditor.kt       # Markdown 编辑（待创建）
```

## 📞 获取帮助

### 常见问题

1. **Gradle 同步慢**：检查网络，或使用国内镜像
2. **缺少 SDK**：打开 SDK Manager 安装
3. **无法运行**：检查 USB 调试或创建虚拟设备

### 参考文档

- Android 开发文档：<https://developer.android.com>
- Hexo 文档：<https://hexo.io>
- Git 教程：<https://git-scm.com/book>

## 🎯 下一步

安装 Android Studio 后，你可以：

1. 立即运行现有项目
2. 开始添加博客管理功能
3. 实现 Markdown 编辑器
4. 集成 Git 上传功能

---

**祝你开发顺利！你的移动端 Hexo 博客管理应用即将诞生！** ✨
