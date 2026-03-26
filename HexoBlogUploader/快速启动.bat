@echo off
echo ========================================
echo Hexo 博客上传器 - 快速启动脚本
echo ========================================
echo.

REM 检查是否在正确的目录
if not exist "app\build.gradle" (
    echo 错误：请在 HexoBlogUploader 目录中运行此脚本
    echo 当前目录：%CD%
    pause
    exit /b 1
)

echo 1. 检查 Java 环境
java -version 2>nul
if %errorlevel% neq 0 (
    echo 错误：Java 未安装或未配置环境变量
    echo 请安装 Java 8 或更高版本
    pause
    exit /b 1
)
echo Java 环境检查通过
echo.

echo 2. 检查 Android SDK
if not exist "%ANDROID_HOME%" (
    echo 警告：ANDROID_HOME 环境变量未设置
    echo 请确保 Android SDK 已正确安装
    echo.
)

echo 3. 清理 Gradle 缓存
call gradlew clean
if %errorlevel% neq 0 (
    echo 警告：Gradle 清理失败，继续尝试构建
    echo.
)

echo 4. 同步 Gradle 依赖
echo 正在同步 Gradle 依赖，请稍候...
call gradlew --refresh-dependencies
if %errorlevel% neq 0 (
    echo 错误：Gradle 依赖同步失败
    echo 请检查网络连接或手动在 Android Studio 中同步
    pause
    exit /b 1
)
echo Gradle 依赖同步成功
echo.

echo 5. 构建应用
echo 正在构建应用，请稍候...
call gradlew build
if %errorlevel% neq 0 (
    echo 错误：应用构建失败
    echo 请检查错误信息并修复
    pause
    exit /b 1
)
echo 应用构建成功
echo.

echo 6. 启动 Android Studio（可选）
set /p start_as="是否要启动 Android Studio？(y/n): "
if /i "%start_as%"=="y" (
    echo 正在启动 Android Studio...
    start "" "C:\Program Files\Android\Android Studio\bin\studio64.exe" .
    if %errorlevel% neq 0 (
        echo 警告：无法启动 Android Studio
        echo 请手动打开 Android Studio 并加载项目
    )
)

echo.
echo ========================================
echo 启动完成！
echo.
echo 下一步操作：
echo 1. 打开 Android Studio（如果尚未打开）
echo 2. 连接 Android 设备或启动模拟器
echo 3. 点击运行按钮（绿色三角形）启动应用
echo 4. 按照应用内的指南配置你的博客
echo.
echo 详细指南请查看 "启动指南.md"
echo ========================================
echo.
pause