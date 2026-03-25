# PowerShell 脚本：下载 Android Studio
# 运行方法：以管理员身份运行 PowerShell，然后执行：.\download_android_studio.ps1

Write-Host "=== Android Studio 下载助手 ===" -ForegroundColor Cyan
Write-Host ""

# 检查是否以管理员身份运行
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "警告：建议以管理员身份运行此脚本！" -ForegroundColor Yellow
    Write-Host "右键点击 PowerShell，选择'以管理员身份运行'" -ForegroundColor Yellow
    Write-Host ""
}

# Android Studio 下载链接（最新稳定版）
$downloadUrl = "https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2025.2.3.9/android-studio-2025.2.3.9-windows.zip"
$fileName = "android-studio-windows.zip"
$downloadPath = Join-Path $env:USERPROFILE "Downloads\$fileName"

Write-Host "1. 正在下载 Android Studio..." -ForegroundColor Green
Write-Host "   下载链接: $downloadUrl" -ForegroundColor Gray
Write-Host "   保存路径: $downloadPath" -ForegroundColor Gray
Write-Host ""

# 询问用户是否继续
$response = Read-Host "是否开始下载？(Y/N)"
if ($response -ne "Y" -and $response -ne "y") {
    Write-Host "下载已取消。" -ForegroundColor Yellow
    exit
}

# 下载文件
try {
    Write-Host "正在下载，请稍候..." -ForegroundColor Cyan
    $progressPreference = 'SilentlyContinue'
    Invoke-WebRequest -Uri $downloadUrl -OutFile $downloadPath
    Write-Host "√ 下载完成！" -ForegroundColor Green
} catch {
    Write-Host "× 下载失败: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "备用方案：请手动访问以下链接下载：" -ForegroundColor Yellow
    Write-Host "https://developer.android.com/studio" -ForegroundColor Cyan
    exit 1
}

Write-Host ""
Write-Host "2. 下载完成！" -ForegroundColor Green
Write-Host "   文件已保存到: $downloadPath" -ForegroundColor Gray
Write-Host "   文件大小: " (Get-Item $downloadPath).Length / 1MB "MB" -ForegroundColor Gray
Write-Host ""

Write-Host "3. 下一步操作：" -ForegroundColor Cyan
Write-Host "   a) 解压下载的 ZIP 文件" -ForegroundColor Gray
Write-Host "   b) 进入解压后的 android-studio 目录" -ForegroundColor Gray
Write-Host "   c) 运行 bin\studio64.exe" -ForegroundColor Gray
Write-Host "   d) 按照安装向导完成安装" -ForegroundColor Gray
Write-Host ""

Write-Host "4. 或者，如果你想使用安装程序版本：" -ForegroundColor Cyan
Write-Host "   请访问: https://developer.android.com/studio" -ForegroundColor Gray
Write-Host "   下载 .exe 安装程序版本" -ForegroundColor Gray
Write-Host ""

Write-Host "5. 安装完成后：" -ForegroundColor Cyan
Write-Host "   a) 启动 Android Studio" -ForegroundColor Gray
Write-Host "   b) 选择 'Open' 项目" -ForegroundColor Gray
Write-Host "   c) 导航到: d:\devofandriod\HexoBlogUploader" -ForegroundColor Gray
Write-Host "   d) 等待 Gradle 同步完成" -ForegroundColor Gray
Write-Host ""

Write-Host "=== 安装指南完成 ===" -ForegroundColor Cyan
Write-Host "详细安装说明请查看 INSTALL_GUIDE.md 文件" -ForegroundColor Gray