# Hexo 博客上传器 - Android 应用

一个用于在手机上管理和上传 Hexo 博客的 Android 应用。

## 功能特性

- 📝 **博客管理**：创建、编辑、删除博客文章
- 🔄 **Git 集成**：使用 JGit 进行博客仓库的同步和上传
- ✍️ **Markdown 编辑器**：内置 Markdown 编辑和预览
- 📁 **文件管理**：上传和管理博客图片等资源
- ☁️ **云端同步**：支持 GitHub、Gitee 等 Git 仓库

## 项目结构

```
HexoBlogUploader/
├── app/                          # 主应用模块
│   ├── src/main/
│   │   ├── java/com/example/hexobloguploader/
│   │   │   ├── MainActivity.kt          # 主活动
│   │   │   ├── BlogManager.kt           # 博客管理
│   │   │   ├── GitOperations.kt         # Git 操作
│   │   │   └── MarkdownEditor.kt        # Markdown 编辑器
│   │   ├── res/                         # 资源文件
│   │   └── AndroidManifest.xml          # 应用清单
│   └── build.gradle                    # 模块级构建配置
├── build.gradle                        # 项目级构建配置
├── settings.gradle                     # 项目设置
├── gradle.properties                   # Gradle 属性
├── INSTALL_GUIDE.md                    # 安装指南
├── download_android_studio.ps1         # 下载脚本
└── README.md                           # 本文件
```

## 快速开始

### 1. 安装 Android Studio

请按照 [INSTALL_GUIDE.md](INSTALL_GUIDE.md) 中的步骤安装 Android Studio。

或者运行下载脚本：

```powershell
# 以管理员身份运行 PowerShell
.\download_android_studio.ps1
```

### 2. 打开项目

1. 启动 Android Studio
2. 选择 "Open"
3. 导航到 `d:\devofandriod\HexoBlogUploader`
4. 点击 "OK"

### 3. 等待项目同步

Android Studio 会自动：

- 下载 Gradle 依赖
- 同步项目配置
- 构建项目

### 4. 运行应用

1. 连接 Android 设备或启动模拟器
2. 点击工具栏的 "Run" 按钮（绿色三角形）
3. 选择目标设备
4. 应用将在设备上运行

## 技术栈

- **语言**：Kotlin
- **最小 SDK**：API 21 (Android 5.0)
- **架构**：MVVM (Model-View-ViewModel)
- **依赖注入**：Hilt (可选)
- **异步处理**：Coroutines + Flow

## 主要依赖

```gradle
dependencies {
    // JGit 库（用于 Git 操作）
    implementation 'org.eclipse.jgit:org.eclipse.jgit:6.7.0.202309050840-r'
    
    // Markdown 渲染库（用于预览）
    implementation 'io.noties.markwon:editor:4.6.2'
    implementation 'io.noties.markwon:core:4.6.2'
    
    // 文件选择器（用于选择图片等）
    implementation 'com.github.angads25:filepicker:1.1.1'
    
    // Android Jetpack 组件
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
}
```

## 核心功能实现计划

### 第一阶段：基础框架

- [x] 项目创建和配置
- [x] 基础界面搭建
- [ ] 博客列表显示
- [ ] 博客详情查看

### 第二阶段：编辑功能

- [ ] Markdown 编辑器
- [ ] 实时预览
- [ ] 图片上传
- [ ] 草稿保存

### 第三阶段：Git 集成

- [ ] Git 仓库配置
- [ ] 文章提交
- [ ] 同步推送
- [ ] 冲突处理

### 第四阶段：高级功能

- [ ] 主题切换
- [ ] 离线编辑
- [ ] 多仓库支持
- [ ] 数据备份

## 权限说明

应用需要以下权限：

- **网络权限**：用于 Git 操作和网络请求
- **存储权限**：用于读写博客文件和图片
- **相机权限**：用于拍照上传（可选）

## 开发说明

### 代码规范

- 使用 Kotlin 官方代码风格
- 遵循 Android 开发最佳实践
- 使用 ViewBinding 替代 findViewById
- 使用 Coroutines 处理异步任务

### 构建配置

- 使用最新稳定版 Android Gradle Plugin
- 启用 ViewBinding 和 DataBinding
- 配置 ProGuard 规则（发布版本）

## 故障排除

### 常见问题

1. **Gradle 同步失败**
   - 检查网络连接
   - 点击 "File" → "Sync Project with Gradle Files"
   - 或点击工具栏的 "Sync" 按钮

2. **缺少 SDK**
   - 点击 "Tools" → "SDK Manager"
   - 安装缺少的 SDK 版本
   - 确保安装了 "Android SDK Build-Tools"

3. **无法运行应用**
   - 确保已启用 USB 调试（真机）
   - 或创建 Android 虚拟设备（AVD）
   - 点击 "Tools" → "AVD Manager" 创建虚拟设备

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证。详见 [LICENSE](LICENSE) 文件。

## 联系方式

如有问题或建议，请：

1. 提交 Issue
2. 或通过电子邮件联系

---

**开始你的移动端 Hexo 博客管理之旅吧！** 🚀
