# Android Studio 使用指南 - Hexo 博客上传器

## 🚀 快速开始

### 第一步：打开项目
1. **启动 Android Studio**
   - 双击桌面上的 Android Studio 图标
   - 或从开始菜单启动

2. **打开项目**
   - 点击 "Open" 或 "Open an Existing Project"
   - 导航到 `d:\devofandriod\HexoBlogUploader`
   - 选择项目文件夹并打开

3. **等待 Gradle 同步**
   - Android Studio 会自动开始同步 Gradle
   - 等待右下角的进度条完成
   - 如果失败，点击 "Sync Now" 重试

### 第二步：配置设备
#### 选项 A：使用物理设备
1. **启用开发者选项**
   ```
   设置 → 关于手机 → 版本号（点击7次）
   ```

2. **启用 USB 调试**
   ```
   设置 → 开发者选项 → USB 调试（开启）
   ```

3. **连接设备**
   - 通过 USB 连接手机到电脑
   - 在手机上允许 USB 调试

#### 选项 B：使用模拟器
1. **打开 AVD Manager**
   - 点击工具栏的 AVD Manager 图标
   - 或通过 Tools → AVD Manager

2. **创建虚拟设备**
   - 点击 "Create Virtual Device"
   - 选择设备型号（推荐 Pixel 5）
   - 选择系统镜像（Android 11+）
   - 完成创建

3. **启动模拟器**
   - 在 AVD Manager 中点击 "Play" 按钮
   - 等待模拟器启动完成

### 第三步：运行应用
1. **选择设备**
   - 在工具栏的设备下拉菜单中选择你的设备
   - 确保设备状态为 "Connected"

2. **运行应用**
   - 点击绿色三角形 "Run" 按钮
   - 或使用快捷键：Shift + F10
   - 等待应用构建和安装

3. **查看结果**
   - 应用会自动在设备上启动
   - 查看 Logcat 输出了解运行状态

## 🔧 项目结构说明

### 核心文件
```
📁 HexoBlogUploader/
├── 📁 app/                          # 主应用模块
│   ├── 📁 src/main/
│   │   ├── 📁 java/com/example/hexobloguploader/
│   │   │   ├── MainActivity.kt          # 主界面
│   │   │   ├── EditPostActivity.kt      # Markdown 编辑器
│   │   │   ├── GitManagerActivity.kt    # Git 管理界面
│   │   │   └── ...                      # 其他核心类
│   │   ├── 📁 res/layout/              # 布局文件
│   │   │   ├── activity_main.xml        # 主界面布局
│   │   │   ├── activity_edit_post.xml   # 编辑器布局
│   │   │   └── activity_git_manager.xml # Git 管理布局
│   │   └── AndroidManifest.xml          # 应用清单文件
├── build.gradle                         # 项目构建配置
├── settings.gradle                      # 项目设置
└── README.md                            # 项目说明
```

### 关键功能模块
1. **MainActivity** - 文章列表和主界面
2. **EditPostActivity** - Markdown 编辑器
3. **GitManagerActivity** - Git 仓库管理
4. **GitOperationsManager** - Git 操作核心
5. **BlogStorageManager** - 本地存储管理
6. **GitHubActionsConfigGenerator** - 自动部署配置

## 📱 应用功能测试

### 基本功能测试
1. **启动应用**
   - 检查应用是否正常启动
   - 查看主界面是否显示

2. **文章列表**
   - 点击文章查看详情
   - 测试滑动和刷新

3. **编辑器功能**
   - 点击 "+" 按钮创建新文章
   - 测试 Markdown 编辑
   - 测试保存功能

### Git 功能测试
1. **Git 管理界面**
   - 点击菜单进入 Git 管理
   - 查看仓库状态

2. **克隆仓库**
   - 点击 "克隆我的博客"
   - 输入 GitHub 信息测试

3. **提交推送**
   - 编辑文章后保存
   - 测试自动提交功能

## 🛠️ 开发工具使用

### Android Studio 快捷键
| 快捷键 | 功能 |
|--------|------|
| Ctrl + Space | 代码补全 |
| Alt + Enter | 快速修复 |
| Ctrl + O | 重写方法 |
| Ctrl + Alt + L | 格式化代码 |
| Shift + F10 | 运行应用 |
| Shift + F9 | 调试应用 |

### 调试技巧
1. **Logcat 查看**
   - 查看应用日志输出
   - 过滤特定标签的日志

2. **断点调试**
   - 在代码行号旁点击设置断点
   - 使用调试模式运行应用

3. **布局检查**
   - 使用 Layout Inspector
   - 查看界面布局层次

## 🔍 常见问题解决

### 问题 1：Gradle 同步失败
**症状：** 项目无法构建，显示 Gradle 错误

**解决方案：**
1. 清理 Gradle 缓存：
   ```bash
   cd d:\devofandriod\HexoBlogUploader
   gradlew clean
   ```

2. 重新同步：
   - File → Sync Project with Gradle Files
   - 或点击工具栏的 "Sync" 按钮

3. 检查网络连接：
   - 确保可以访问 Maven 仓库
   - 可能需要配置代理

### 问题 2：设备无法连接
**症状：** 设备列表中看不到设备

**解决方案：**
1. 检查 USB 调试：
   - 确保开发者选项已启用
   - 确保 USB 调试已开启

2. 重启 ADB：
   ```bash
   adb kill-server
   adb start-server
   ```

3. 更换 USB 线或端口

### 问题 3：应用崩溃
**症状：** 应用启动后立即崩溃

**解决方案：**
1. 查看 Logcat 错误信息
2. 检查 AndroidManifest.xml 权限
3. 检查依赖库版本兼容性

### 问题 4：Git 操作失败
**症状：** 克隆或提交操作失败

**解决方案：**
1. 检查网络连接
2. 验证 GitHub Token 是否正确
3. 检查仓库地址格式

## 📊 性能优化建议

### 构建优化
1. **启用构建缓存**
   ```gradle
   android {
       buildFeatures {
           buildConfig true
       }
   }
   ```

2. **使用最新 Gradle 版本**
   - 定期更新 Gradle 插件
   - 使用最新 Android SDK

### 代码优化
1. **使用协程处理异步任务**
2. **避免主线程阻塞操作**
3. **使用 ViewBinding 替代 findViewById**

## 🎯 下一步开发计划

### 短期目标（1-2周）
1. ✅ 完成基本功能开发
2. ✅ 实现 Git 集成
3. ✅ 添加 GitHub Actions 支持
4. 🔄 测试和 bug 修复

### 中期目标（1-2月）
1. 添加图片上传功能
2. 实现主题配置编辑器
3. 添加离线模式支持
4. 优化用户体验

### 长期目标（3-6月）
1. 支持多平台同步
2. 添加数据分析功能
3. 发布到 Google Play Store
4. 社区功能开发

## 📚 学习资源

### 官方文档
- [Android 开发者文档](https://developer.android.com)
- [Kotlin 官方文档](https://kotlinlang.org/docs/)
- [Android Studio 用户指南](https://developer.android.com/studio/intro)

### 在线课程
- [Android 开发基础](https://developer.android.com/courses)
- [Kotlin 编程语言](https://kotlinlang.org/docs/tutorials/)
- [Git 版本控制](https://git-scm.com/doc)

### 社区支持
- [Stack Overflow](https://stackoverflow.com/questions/tagged/android)
- [GitHub Issues](https://github.com/hongming351/devofandriod/issues)
- [Android 开发者社区](https://developer.android.com/community)

## 🎉 恭喜！

你已经成功设置了 Android Studio 并准备好开发 Hexo 博客上传器应用。现在你可以：

1. **运行应用**：测试现有功能
2. **修改代码**：根据需求定制功能
3. **添加功能**：实现新的特性
4. **发布应用**：分享你的作品

**祝你开发顺利！如果有任何问题，请参考本文档或联系开发者。**

---

### 快速命令参考
```bash
# 清理项目
gradlew clean

# 构建应用
gradlew build

# 运行测试
gradlew test

# 生成 APK
gradlew assembleRelease

# 安装应用到设备
adb install app/build/outputs/apk/debug/app-debug.apk