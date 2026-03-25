@echo off
echo ========================================
echo    Android Studio 直接下载工具
echo ========================================
echo.
echo 这个工具将帮助你下载 Android Studio。
echo.
echo 注意：Android Studio 安装文件大约 1GB，下载需要时间。
echo.
echo 请选择下载选项：
echo.
echo 1. 下载 .exe 安装程序（推荐）
echo 2. 下载 .zip 便携版本
echo 3. 手动访问网站下载
echo.
set /p choice="请选择 (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo 正在下载 Android Studio 安装程序...
    echo 文件大小：约 1GB
    echo 下载时间取决于你的网络速度...
    echo.
    curl -L -o "%USERPROFILE%\Downloads\android-studio-installer.exe" "https://redirector.gvt1.com/edgedl/android/studio/install/2025.2.3.9/android-studio-2025.2.3.9-windows.exe"
    echo.
    echo 下载完成！
    echo 文件保存在：%USERPROFILE%\Downloads\android-studio-installer.exe
    echo.
    echo 请运行下载的安装程序完成安装。
)

if "%choice%"=="2" (
    echo.
    echo 正在下载 Android Studio ZIP 版本...
    echo 文件大小：约 1GB
    echo 下载时间取决于你的网络速度...
    echo.
    curl -L -o "%USERPROFILE%\Downloads\android-studio.zip" "https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2025.2.3.9/android-studio-2025.2.3.9-windows.zip"
    echo.
    echo 下载完成！
    echo 文件保存在：%USERPROFILE%\Downloads\android-studio.zip
    echo.
    echo 请解压 ZIP 文件，然后运行 bin\studio64.exe
)

if "%choice%"=="3" (
    echo.
    echo 正在打开 Android Studio 官方网站...
    start https://developer.android.com/studio
    echo.
    echo 请在浏览器中手动下载 Android Studio。
)

echo.
echo ========================================
echo    下载完成！
echo ========================================
echo.
echo 安装步骤：
echo 1. 运行下载的安装程序（或解压 ZIP 文件）
echo 2. 按照安装向导完成安装
echo 3. 首次运行选择 "Standard" 安装
echo 4. 等待 SDK 组件下载
echo 5. 打开项目：d:\devofandriod\HexoBlogUploader
echo.
echo 按任意键退出...
pause >nul