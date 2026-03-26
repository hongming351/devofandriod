# Hexo 博客上传器 - 快速启动脚本 (PowerShell 版本)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Hexo 博客上传器 - 快速启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查是否在正确的目录
if (-not (Test-Path "app\build.gradle")) {
    Write-Host "错误：请在 HexoBlogUploader 目录中运行此脚本" -ForegroundColor Red
    Write-Host "当前目录：$PWD" -ForegroundColor Yellow
    Read-Host "按 Enter 键退出"
    exit 1
}

Write-Host "1. 检查 Java 环境" -ForegroundColor Green
try {
    $javaVersion = java -version 2>&1
    Write-Host "Java 环境检查通过" -ForegroundColor Green
    Write-Host "Java 版本信息：" -ForegroundColor Gray
    $javaVersion | ForEach-Object { Write-Host "  $_" -ForegroundColor Gray }
} catch {
    Write-Host "错误：Java 未安装或未配置环境变量" -ForegroundColor Red
    Write-Host "请安装 Java 8 或更高版本" -ForegroundColor Yellow
    Read-Host "按 Enter 键退出"
    exit 1
}
Write-Host ""

Write-Host "2. 检查 Android SDK" -ForegroundColor Green
if (-not $env:ANDROID_HOME) {
    Write-Host "警告：ANDROID_HOME 环境变量未设置" -ForegroundColor Yellow
    Write-Host "请确保 Android SDK 已正确安装" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "3. 清理 Gradle 缓存" -ForegroundColor Green
try {
    & .\gradlew clean
    Write-Host "Gradle 清理成功" -ForegroundColor Green
} catch {
    Write-Host "警告：Gradle 清理失败，继续尝试构建" -ForegroundColor Yellow
}
Write-Host ""

Write-Host "4. 同步 Gradle 依赖" -ForegroundColor Green
Write-Host "正在同步 Gradle 依赖，请稍候..." -ForegroundColor Gray
try {
    & .\gradlew --refresh-dependencies
    Write-Host "Gradle 依赖同步成功" -ForegroundColor Green
} catch {
    Write-Host "错误：Gradle 依赖同步失败" -ForegroundColor Red
    Write-Host "请检查网络连接或手动在 Android Studio 中同步" -ForegroundColor Yellow
    Read-Host "按 Enter 键退出"
    exit 1
}
Write-Host ""

Write-Host "5. 构建应用" -ForegroundColor Green
Write-Host "正在构建应用，请稍候..." -ForegroundColor Gray
try {
    & .\gradlew build
    Write-Host "应用构建成功" -ForegroundColor Green
} catch {
    Write-Host "错误：应用构建失败" -ForegroundColor Red
    Write-Host "请检查错误信息并修复" -ForegroundColor Yellow
    Read-Host "按 Enter 键退出"
    exit 1
}
Write-Host ""

Write-Host "6. 启动 Android Studio（可选）" -ForegroundColor Green
$startAS = Read-Host "是否要启动 Android Studio？(y/n)"
if ($startAS -eq 'y' -or $startAS -eq 'Y') {
    Write-Host "正在启动 Android Studio..." -ForegroundColor Gray
    $studioPath = "C:\Program Files\Android\Android Studio\bin\studio64.exe"
    if (Test-Path $studioPath) {
        Start-Process $studioPath -ArgumentList "."
        Write-Host "Android Studio 已启动" -ForegroundColor Green
    } else {
        Write-Host "警告：无法找到 Android Studio" -ForegroundColor Yellow
        Write-Host "请手动打开 Android Studio 并加载项目" -ForegroundColor Yellow
        Write-Host "Android Studio 可能安装在其他位置" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "启动完成！" -ForegroundColor Green
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Yellow
Write-Host "1. 打开 Android Studio（如果尚未打开）" -ForegroundColor Gray
Write-Host "2. 连接 Android 设备或启动模拟器" -ForegroundColor Gray
Write-Host "3. 点击运行按钮（绿色三角形）启动应用" -ForegroundColor Gray
Write-Host "4. 按照应用内的指南配置你的博客" -ForegroundColor Gray
Write-Host ""
Write-Host "详细指南请查看 '启动指南.md'" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Read-Host "按 Enter 键完成"