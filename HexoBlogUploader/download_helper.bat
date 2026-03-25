@echo off
chcp 65001 >nul
echo ========================================
echo    Android Studio Download Helper
echo ========================================
echo.
echo This tool will help you download Android Studio.
echo.
echo Note: Android Studio installer is about 1GB in size.
echo.
echo Please choose download option:
echo.
echo 1. Download .exe installer (Recommended)
echo 2. Download .zip portable version
echo 3. Open website for manual download
echo.
set /p choice="Enter choice (1/2/3): "

if "%choice%"=="1" (
    echo.
    echo Downloading Android Studio installer...
    echo File size: about 1GB
    echo Download time depends on your internet speed...
    echo.
    curl -L -o "%USERPROFILE%\Downloads\android-studio.exe" "https://redirector.gvt1.com/edgedl/android/studio/install/2025.2.3.9/android-studio-2025.2.3.9-windows.exe"
    echo.
    echo Download complete!
    echo File saved to: %USERPROFILE%\Downloads\android-studio.exe
    echo.
    echo Please run the downloaded installer.
)

if "%choice%"=="2" (
    echo.
    echo Downloading Android Studio ZIP version...
    echo File size: about 1GB
    echo Download time depends on your internet speed...
    echo.
    curl -L -o "%USERPROFILE%\Downloads\android-studio.zip" "https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2025.2.3.9/android-studio-2025.2.3.9-windows.zip"
    echo.
    echo Download complete!
    echo File saved to: %USERPROFILE%\Downloads\android-studio.zip
    echo.
    echo Please extract the ZIP file and run bin\studio64.exe
)

if "%choice%"=="3" (
    echo.
    echo Opening Android Studio website...
    start https://developer.android.com/studio
    echo.
    echo Please download Android Studio manually from the website.
)

echo.
echo ========================================
echo    Download Complete!
echo ========================================
echo.
echo Installation steps:
echo 1. Run the downloaded installer (or extract ZIP)
echo 2. Follow the installation wizard
echo 3. On first run, choose "Standard" installation
echo 4. Wait for SDK components to download
echo 5. Open project: d:\devofandriod\HexoBlogUploader
echo.
echo Press any key to exit...
pause >nul