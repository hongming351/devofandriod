# Hexo 博客上传器 - Android Studio 安装指南

## 1. 下载 Android Studio

1. 访问官方网站：https://developer.android.com/studio
2. 点击 "Download Android Studio" 按钮
3. 下载 Windows 版本（文件大小约 1GB）

## 2. 安装 Android Studio

1. 运行下载的安装程序 `android-studio-*.exe`
2. 按照安装向导的步骤进行：
   - 点击 "Next"
   - 选择安装组件（建议全选）
   - 选择安装路径（建议使用默认路径）
   - 点击 "Install"
   - 等待安装完成
   - 点击 "Finish" 启动 Android Studio

## 3. 首次运行配置

1. **导入设置**：如果是首次安装，选择 "Do not import settings"
2. **安装类型**：选择 "Standard"（标准安装）
3. **主题选择**：选择你喜欢的主题（Light 或 Dark）
4. **SDK 组件下载**：
   - Android Studio 会自动下载必要的 SDK 组件
   - 这可能需要一些时间（10-30分钟，取决于网络速度）
   - 确保有稳定的网络连接

## 4. 打开项目

1. 启动 Android Studio 后，选择 "Open"
2. 导航到项目目录：`d:\devofandriod\HexoBlogUploader`
3. 点击 "OK"

## 5. 项目配置

项目首次打开时，Android Studio 会自动：
- 下载 Gradle 依赖
- 同步项目
- 构建项目

## 6. 运行应用

1. 连接 Android 设备或启动模拟器
2. 点击工具栏的 "Run" 按钮（绿色三角形）
3. 选择目标设备
4. 应用将在设备上运行

## 7. 项目结构说明

```
HexoBlogUploader/
├── app/                    # 主应用模块
│   ├── src/main/
│   │   ├── java/com/example/hexobloguploader/
│   │   │   └── MainActivity.kt    # 主活动
│   │   ├── res/                   # 资源文件
│   │   └── AndroidManifest.xml    # 应用清单
│   └── build.gradle              # 模块级构建配置
├── build.gradle                  # 项目级构建配置
├── settings.gradle              # 项目设置
└── gradle.properties            # Gradle 属性
```

## 8. 已配置的依赖

项目已配置以下依赖：
- **JGit 6.7.0**：用于 Git 操作（博客上传）
- **Markwon 4.6.2**：用于 Markdown 渲染和编辑
- **FilePicker 1.1.1**：用于文件选择

## 9. 后续开发

1. **实现博客管理功能**：添加博客列表、编辑、发布功能
2. **集成 Git 操作**：实现 clone、pull、push、commit 等操作
3. **添加 Markdown 编辑器**：实现博客内容编辑和预览
4. **文件管理**：实现图片上传和文件管理

## 10. 故障排除

### 问题：Gradle 同步失败
**解决方案**：
1. 检查网络连接
2. 点击 "File" → "Sync Project with Gradle Files"
3. 或点击工具栏的 "Sync" 按钮

### 问题：缺少 SDK
**解决方案**：
1. 点击 "Tools" → "SDK Manager"
2. 安装缺少的 SDK 版本
3. 确保安装了 "Android SDK Build-Tools"

### 问题：无法运行应用
**解决方案**：
1. 确保已启用 USB 调试（真机）
2. 或创建 Android 虚拟设备（AVD）
3. 点击 "Tools" → "AVD Manager" 创建虚拟设备

## 联系与支持

如有问题，请参考：
- Android Studio 官方文档：https://developer.android.com/studio/intro
- 项目 GitHub 仓库（待创建）
- Hexo 官方文档：https://hexo.io/