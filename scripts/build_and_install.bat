@echo off
chcp 65001 > nul
pushd %~dp0..
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build_and_install.ps1"
if %ERRORLEVEL% NEQ 0 (
    echo.
    popd
    pause
    exit /b %ERRORLEVEL%
)
popd
pause
