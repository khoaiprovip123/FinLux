# Script build va nap APK FinLux vao dien thoai dang ket noi ADB
$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "   FINLUX - BUILD & INSTALL APK TO DEVICE  " -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 0. Thiết lập biến môi trường Java & Android SDK
$toolchainSdk = "C:\Users\$env:USERNAME\.cache\finlux-toolchain\android-sdk"
$stdSdk = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
if (Test-Path $toolchainSdk) {
    $env:ANDROID_HOME = $toolchainSdk
    $env:ANDROID_SDK_ROOT = $toolchainSdk
} elseif (Test-Path $stdSdk) {
    $env:ANDROID_HOME = $stdSdk
    $env:ANDROID_SDK_ROOT = $stdSdk
}

$toolchainJdk = "C:\Users\$env:USERNAME\.cache\finlux-toolchain\jdk\jdk-17.0.20+8"
if (Test-Path $toolchainJdk) {
    $env:JAVA_HOME = $toolchainJdk
    $env:PATH = "$toolchainJdk\bin;$env:PATH"
}

if ($env:ANDROID_HOME -and (Test-Path "$env:ANDROID_HOME\platform-tools")) {
    $env:PATH = "$env:ANDROID_HOME\platform-tools;$env:PATH"
}

$localProps = "local.properties"
if (-not (Test-Path $localProps)) {
    $sdkEscaped = $env:ANDROID_HOME -replace "\\", "/"
    "sdk.dir=$sdkEscaped" | Out-File -FilePath $localProps -Encoding ascii
    Write-Host "[Info] Da khoi tao local.properties tro den Android SDK." -ForegroundColor Green
}

# 1. Kiem tra ADB va thiet bi Android đang kết nối
Write-Host "`n[1/3] Kiem tra thiet bi ADB dang ket noi..." -ForegroundColor Yellow
try {
    $adbOutput = adb devices
} catch {
    Write-Host "❌ KHONG TIM THAY LENH 'adb'. Vui long kiem tra Android SDK / Platform-Tools co trong PATH." -ForegroundColor Red
    exit 1
}

$targetDevices = @()
foreach ($line in $adbOutput) {
    if ($line -match "^([^\s]+)\s+device$") {
        $targetDevices += $matches[1]
    }
}

if ($targetDevices.Count -eq 0) {
    Write-Host "❌ KHONG TIM THAY THIET BI ANDROID NAO DANG KET NOI ADB!" -ForegroundColor Red
    Write-Host "Vui long kiem tra:" -ForegroundColor Yellow
    Write-Host "  1. Cap USB da ket noi voi PC"
    Write-Host "  2. Che do 'USB Debugging' (Sửa lỗi USB) da bat tren dien thoai"
    Write-Host "  3. Ban da cho phép (Allow) máy tính truy cập ADB tren màn hình dien thoai"
    exit 1
}

$targetDevices | ForEach-Object { Write-Host "   -> Thiet bi tim thay: $_" -ForegroundColor Green }

# 1.5 Auto Version Bump trong app/build.gradle.kts
$gradleFile = "app/build.gradle.kts"
if (Test-Path $gradleFile) {
    $utf8NoBom = [System.Text.UTF8Encoding]::new($false)
    $content = [System.IO.File]::ReadAllText((Resolve-Path $gradleFile), $utf8NoBom)
    if ($content -match "versionCode\s*=\s*(\d+)") {
        $oldCode = [int]$matches[1]
        $newCode = $oldCode + 1
        $content = $content -replace "versionCode\s*=\s*\d+", "versionCode = $newCode"
        [System.IO.File]::WriteAllText((Resolve-Path $gradleFile), $content, $utf8NoBom)
        Write-Host "   -> Auto Version Bump: versionCode $oldCode -> $newCode" -ForegroundColor Cyan
    }
}

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

# 3. Cài đè (Replace/Update) APK trực tiếp lên từng thiết bị
Write-Host "`n[3/3] Dang nap APK moi vao cac thiet bi Android..." -ForegroundColor Yellow
$successCount = 0
foreach ($devId in $targetDevices) {
    Write-Host "   -> Dang nap va open app tren [$devId]..." -ForegroundColor Cyan
    adb -s $devId install -r -t -d $apkPath
    if ($LASTEXITCODE -eq 0) {
        $successCount++
        adb -s $devId shell am start -n com.finlux.app/.MainActivity | Out-Null
        Write-Host "      ✅ Thanh cong: $devId" -ForegroundColor Green
    } else {
        Write-Host "      ❌ That bai: $devId" -ForegroundColor Red
    }
}

if ($successCount -gt 0) {
    Write-Host "`n==========================================" -ForegroundColor Green
    Write-Host "   ✅ DA NAP APK KHOP THANH CONG CHO $successCount THIET BI!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
} else {
    Write-Host "`n❌ NAP APK THAT BAI TREN TAT CA THIET BI!" -ForegroundColor Red
}
