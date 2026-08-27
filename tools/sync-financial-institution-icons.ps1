[CmdletBinding()]
param(
    [string]$ApiUrl = 'https://api.vietqr.io/v2/banks'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$resourceDirectory = Join-Path $projectRoot 'app\src\main\res\drawable-nodpi'
$manifestDirectory = Join-Path $projectRoot 'docs\data'
$manifestPath = Join-Path $manifestDirectory 'vietqr-financial-institutions.json'

New-Item -ItemType Directory -Path $resourceDirectory -Force | Out-Null
New-Item -ItemType Directory -Path $manifestDirectory -Force | Out-Null

$response = Invoke-RestMethod -Uri $ApiUrl -Method Get
if ($response.code -ne '00' -or -not $response.data) {
    throw "VietQR bank API returned an invalid response: $($response.desc)"
}

Add-Type -AssemblyName System.Drawing
$downloaded = 0
$failed = [System.Collections.Generic.List[string]]::new()

foreach ($institution in $response.data) {
    $resourceSuffix = ($institution.bin.ToString().ToLowerInvariant() -replace '[^a-z0-9_]', '_')
    $resourceName = "ic_vietqr_$resourceSuffix.png"
    $targetPath = Join-Path $resourceDirectory $resourceName
    $temporaryPath = [System.IO.Path]::GetTempFileName()

    try {
        Invoke-WebRequest -Uri $institution.logo -OutFile $temporaryPath
        if ((Get-Item -LiteralPath $temporaryPath).Length -lt 128) {
            throw 'Downloaded file is unexpectedly small.'
        }

        $image = [System.Drawing.Image]::FromFile($temporaryPath)
        try {
            if ($image.Width -lt 16 -or $image.Height -lt 16) {
                throw "Invalid image dimensions: $($image.Width)x$($image.Height)"
            }
        }
        finally {
            $image.Dispose()
        }

        Move-Item -LiteralPath $temporaryPath -Destination $targetPath -Force
        $downloaded++
    }
    catch {
        $failed.Add("$($institution.shortName): $($_.Exception.Message)")
        Remove-Item -LiteralPath $temporaryPath -Force -ErrorAction SilentlyContinue
    }
}

$manifest = [ordered]@{
    schemaVersion = 1
    source = $ApiUrl
    syncedAt = (Get-Date).ToUniversalTime().ToString('o')
    count = $response.data.Count
    institutions = $response.data
}
$manifest | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $manifestPath -Encoding utf8

Write-Host "Downloaded $downloaded/$($response.data.Count) institution logos."
Write-Host "Manifest: $manifestPath"

if ($failed.Count -gt 0) {
    $failed | ForEach-Object { Write-Error $_ }
    throw "$($failed.Count) institution logo(s) could not be downloaded."
}

