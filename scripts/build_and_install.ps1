# Script build va nap APK FinLux vao dien thoai dang ket noi ADB
$ErrorActionPreference = "Stop"
Set-Location (Join-Path $PSScriptRoot "..")

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

# 0.5 Kiem tra va khoi phuc Firebase Configuration & Keystore
$googleServicesPath = "app/google-services.json"
if (-not (Test-Path $googleServicesPath)) {
    $embeddedJsonB64 = "ewogICJwcm9qZWN0X2luZm8iOiB7CiAgICAicHJvamVjdF9udW1iZXIiOiAiOTI3NzUxNzUzOTYyIiwKICAgICJmaXJlYmFzZV91cmwiOiAiaHR0cHM6Ly9maW5sdXgtZDAyOTctZGVmYXVsdC1ydGRiLmZpcmViYXNlaW8uY29tIiwKICAgICJwcm9qZWN0X2lkIjogImZpbmx1eC1kMDI5NyIsCiAgICAic3RvcmFnZV9idWNrZXQiOiAiZmlubHV4LWQwMjk3LmZpcmViYXNlc3RvcmFnZS5hcHAiCiAgfSwKICAiY2xpZW50IjogWwogICAgewogICAgICAiY2xpZW50X2luZm8iOiB7CiAgICAgICAgIm1vYmlsZXNka19hcHBfaWQiOiAiMTo5Mjc3NTE3NTM5NjI6YW5kcm9pZDpmY2NiMTE4NDhmNDFlZWM1ZmM4NzkzIiwKICAgICAgICAiYW5kcm9pZF9jbGllbnRfaW5mbyI6IHsKICAgICAgICAgICJwYWNrYWdlX25hbWUiOiAiY29tLmZpbmx1eC5hcHAiCiAgICAgICAgfQogICAgICB9LAogICAgICAib2F1dGhfY2xpZW50IjogWwogICAgICAgIHsKICAgICAgICAgICJjbGllbnRfaWQiOiAiOTI3NzUxNzUzOTYyLXQ4amVkOXRmbjBtOGtqZzE5djc1ZzdiZjY4ZjNkNmg2LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwKICAgICAgICAgICJjbGllbnRfdHlwZSI6IDEsCiAgICAgICAgICAiYW5kcm9pZF9pbmZvIjogewogICAgICAgICAgICAicGFja2FnZV9uYW1lIjogImNvbS5maW5sdXguYXBwIiwKICAgICAgICAgICAgImNlcnRpZmljYXRlX2hhc2giOiAiZWFhOWVhYWJiN2I5OWExZmYxODE2NGJmNzYyZWUxNzVjNTMyN2Y0NyIKICAgICAgICAgIH0KICAgICAgICB9LAogICAgICAgIHsKICAgICAgICAgICJjbGllbnRfaWQiOiAiOTI3NzUxNzUzOTYyLTA0cGFvbjJ0ZXJta2JlYW5ic3Y3bTh0OWE4bTZ0azVoLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwKICAgICAgICAgICJjbGllbnRfdHlwZSI6IDMKICAgICAgICB9CiAgICAgIF0sCiAgICAgICJhcGlfa2V5IjogWwogICAgICAgIHsKICAgICAgICAgICJjdXJyZW50X2tleSI6ICJBSXphU3lBeUpGOUhPelp3SVo2X01hRk40SVNlV0lTRlVSdWlKSkUiCiAgICAgICAgfQogICAgICBdLAogICAgICAic2VydmljZXMiOiB7CiAgICAgICAgImFwcGludml0ZV9zZXJ2aWNlIjogewogICAgICAgICAgIm90aGVyX3BsYXRmb3JtX29hdXRoX2NsaWVudCI6IFsKICAgICAgICAgICAgewogICAgICAgICAgICAgICJjbGllbnRfaWQiOiAiOTI3NzUxNzUzOTYyLTA0cGFvbjJ0ZXJta2JlYW5ic3Y3bTh0OWE4bTZ0azVoLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwKICAgICAgICAgICAgICAiY2xpZW50X3R5cGUiOiAzCiAgICAgICAgICAgIH0KICAgICAgICAgIF0KICAgICAgICB9CiAgICAgIH0KICAgIH0KICBdLAogICJjb25maWd1cmF0aW9uX3ZlcnNpb24iOiAiMSIKfQo="
    [IO.File]::WriteAllBytes($googleServicesPath, [Convert]::FromBase64String($embeddedJsonB64))
    Write-Host "[Info] Da tu dong khoi phuc app/google-services.json tu cau hinh goc." -ForegroundColor Green
}

$keystorePath = "gradle/debug.keystore"
if (-not (Test-Path $keystorePath)) {
    if (-not (Test-Path "gradle")) { New-Item -ItemType Directory -Path "gradle" | Out-Null }
    $keystoreB64 = "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"
    [IO.File]::WriteAllBytes($keystorePath, [Convert]::FromBase64String($keystoreB64))
    Write-Host "[Info] Da tu dong khoi phuc gradle/debug.keystore khop voi Google SHA-1." -ForegroundColor Green
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
    Write-Host "⚠️ KHONG TIM THAY THIET BI ANDROID NAO DANG KET NOI ADB!" -ForegroundColor Yellow
    Write-Host "   -> Tien trinh se tiep tuc BUILD APK (bo qua buoc tu dong nap qua ADB)." -ForegroundColor DarkYellow
} else {
    $targetDevices | ForEach-Object { Write-Host "   -> Thiet bi tim thay: $_" -ForegroundColor Green }
}

# 1.5 Hien thi thong tin phien ban hien tai tu app/build.gradle.kts (Khong tu dong tang versionCode)
$gradleFile = "app/build.gradle.kts"
if (Test-Path $gradleFile) {
    $content = Get-Content $gradleFile -Raw
    $vName = if ($content -match 'versionName\s*=\s*"([^"]+)"') { $matches[1] } else { "N/A" }
    $vCode = if ($content -match 'versionCode\s*=\s*(\d+)') { $matches[1] } else { "N/A" }
    Write-Host "   -> Thong tin phien ban: v$vName (versionCode $vCode)" -ForegroundColor Cyan
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

$apkItem = Get-Item $apkPath
$apkSizeMb = [math]::Round($apkItem.Length / 1MB, 2)
Write-Host "   ✅ Build APK thanh cong: $apkPath ($apkSizeMb MB)" -ForegroundColor Green

# 3. Cài đè (Replace/Update) APK trực tiếp lên từng thiết bị (neu co)
Write-Host "`n[3/3] Nap APK vao thiet bi Android..." -ForegroundColor Yellow
if ($targetDevices.Count -eq 0) {
    Write-Host "ℹ️ Bo qua buoc nap tu dong vi khong co thiet bi ADB nao dang ket noi." -ForegroundColor Yellow
    Write-Host "📦 File APK da san sang tai: $(Resolve-Path $apkPath)" -ForegroundColor Cyan
    Write-Host "`n==========================================" -ForegroundColor Green
    Write-Host "   ✅ BUILD APK HOAN TAT!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
    exit 0
}

$successCount = 0
foreach ($devId in $targetDevices) {
    Write-Host "   -> Dang nap va open app tren [$devId]..." -ForegroundColor Cyan
    $output = adb -s $devId install -r -t -d $apkPath 2>&1 | Out-String
    if ($output -match "Success") {
        $successCount++
        adb -s $devId shell am start -n com.finlux.app/.MainActivity | Out-Null
        Write-Host "      ✅ Thanh cong: $devId" -ForegroundColor Green
    } else {
        if ($output -match "signatures do not match" -or $output -match "INSTALL_FAILED_UPDATE_INCOMPATIBLE" -or $output -match "INSTALL_FAILED_VERSION_DOWNGRADE") {
            Write-Host "      ⚠️ Phat hien xung dot ban cai cu tren [$devId], dang go va nap lai..." -ForegroundColor Yellow
            adb -s $devId uninstall com.finlux.app | Out-Null
            $retryOutput = adb -s $devId install -r -t -d $apkPath 2>&1 | Out-String
            if ($retryOutput -match "Success") {
                $successCount++
                adb -s $devId shell am start -n com.finlux.app/.MainActivity | Out-Null
                Write-Host "      ✅ Thanh cong (sau khi go ban cu): $devId" -ForegroundColor Green
            } else {
                Write-Host "      ❌ That bai: $devId -> $retryOutput" -ForegroundColor Red
            }
        } else {
            Write-Host "      ❌ That bai: $devId -> $output" -ForegroundColor Red
        }
    }
}

if ($successCount -gt 0) {
    Write-Host "`n==========================================" -ForegroundColor Green
    Write-Host "   ✅ DA NAP APK KHOP THANH CONG CHO $successCount THIET BI!" -ForegroundColor Green
    Write-Host "==========================================" -ForegroundColor Green
} else {
    Write-Host "`n❌ NAP APK THAT BAI TREN TAT CA THIET BI!" -ForegroundColor Red
}
