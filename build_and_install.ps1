# Script build va nap APK FinLux vao dien thoai dang ket noi ADB
$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   FINLUX - BUILD & INSTALL APK TO DEVICE  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 0. Thiết lập biến môi trường Android SDK
$androidSdkPath = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
if (Test-Path $androidSdkPath) {
    $env:ANDROID_HOME = $androidSdkPath
    $env:ANDROID_SDK_ROOT = $androidSdkPath
}

$localProps = "local.properties"
if (-not (Test-Path $localProps)) {
    "sdk.dir=C:/Users/$env:USERNAME/AppData/Local/Android/Sdk" | Out-File -FilePath $localProps -Encoding ascii
    Write-Host "[Info] Da khoi tao local.properties tro den Android SDK." -ForegroundColor Green
}

# 1. Kiem tra ADB va thiet bi Android đang kết nối
Write-Host "`n[1/3] Kiem tra thiet bi ADB dang ket noi..." -ForegroundColor Yellow
try {
    $adbCheck = adb devices
} catch {
    Write-Host "❌ KHONG TIM THAY LENH 'adb'. Vui long kiem tra Android SDK / Platform-Tools co trong PATH." -ForegroundColor Red
    exit 1
}

$devices = adb devices | Select-String -Pattern "\tdevice$"
if (-not $devices) {
    Write-Host "❌ KHONG TIM THAY THIET BI ANDROID NAO DANG KET NOI ADB!" -ForegroundColor Red
    Write-Host "Vui long kiem tra:" -ForegroundColor Yellow
    Write-Host "  1. Cap USB da ket noi voi PC"
    Write-Host "  2. Che do 'USB Debugging' (Sửa lỗi USB) da bat tren dien thoai"
    Write-Host "  3. Ban da cho phép (Allow) máy tính truy cập ADB tren màn hình dien thoai"
    exit 1
}

$devices | ForEach-Object { Write-Host "   -> Thiet bi tim thay: $_" -ForegroundColor Green }

# 2. Build APK bang Gradle Wrapper
Write-Host "`n[2/3] Dang build APK Debug..." -ForegroundColor Yellow
cmd /c "gradlew.bat assembleDebug"
if ($LASTEXITCODE -ne 0) {
    Write-Host "`n❌ BUILD THAT BAI! Vui long kiem tra log Gradle o tren." -ForegroundColor Red
    exit 1
}

$apkPath = "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $apkPath)) {
    Write-Host "`n❌ KHONG TIM THAY FILE APK TAI: $apkPath" -ForegroundColor Red
    exit 1
}

# 3. Cài đè (Replace/Update) APK trực tiếp mà không gỡ ứng dụng cũ để không bị MIUI hỏi lại popup Allow
Write-Host "`n[3/3] Dang nap APK moi vao dien thoai (Replace in-place)..." -ForegroundColor Yellow
adb install -r -t -d $apkPath
if ($LASTEXITCODE -eq 0) {
    Write-Host "`n==========================================" -ForegroundColor Green
    Write-Host "   ✅ NAP APK THANH CONG!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    Write-Host "Dang tu dong mo app FinLux tren dien thoai..." -ForegroundColor Yellow
    adb shell am start -n com.finlux.app/.MainActivity
} else {
    Write-Host "`n❌ NAP APK THAT BAI! Vui long kiem tra ket noi USB." -ForegroundColor Red
}
