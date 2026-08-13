@echo off
chcp 65001 > nul
echo ==========================================
echo    FINLUX - BUILD ^& INSTALL APK TO DEVICE
echo ==========================================

set "ANDROID_HOME=C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
set "ANDROID_SDK_ROOT=C:\Users\%USERNAME%\AppData\Local\Android\Sdk"

if not exist local.properties (
    echo sdk.dir=C:/Users/%USERNAME%/AppData/Local/Android/Sdk > local.properties
    echo [Info] Da tao local.properties
)

echo.
echo [1/3] Kiem tra thiet bi ADB dang ket noi...
adb devices > temp_devices.txt 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Khong tim thay lenh 'adb'. Vui long kiem tra Android SDK / Platform-Tools co trong PATH.
    del temp_devices.txt > nul 2>&1
    pause
    exit /b %ERRORLEVEL%
)

type temp_devices.txt | findstr /R "\<device\>" > nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ KHONG TIM THAY THIET BI ANDROID NAO DANG KET NOI ADB!
    echo Vui long kiem tra:
    echo   1. Cap USB va che do USB Debugging tren dien thoai.
    echo   2. Da dong y (Allow) xac nhan ket noi ADB tren dien thoai.
    del temp_devices.txt > nul 2>&1
    pause
    exit /b 1
)
del temp_devices.txt > nul 2>&1
echo   - Da ket noi thiet bi ADB.

echo.
echo [2/3] Dang build APK Debug...
call gradlew.bat assembleDebug
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build thất bại! Vui lòng kiểm tra lỗi bên trên.
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Dang nap APK vao dien thoai...
adb install -r -t -d app\build\outputs\apk\debug\app-debug.apk
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo    ✅ NAP APK THANH CONG!
    echo ==========================================
    echo Dang tu dong mo app FinLux tren dien thoai...
    adb shell am start -n com.finlux.app/.MainActivity
) else (
    echo ❌ Nạp APK thất bại!
)

pause
