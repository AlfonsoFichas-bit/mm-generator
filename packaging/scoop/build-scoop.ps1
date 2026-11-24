<#
.SYNOPSIS
  Construye la distribucion portable de MM Generator para Scoop (Windows).
.DESCRIPTION
  Ejecuta installDist, empaqueta con jpackage (runtime incluido) y genera el ZIP
  esperado por el manifest de Scoop, actualizando el hash en bucket/mm-generator.json.
#>
param(
    [string]$Version
)

$ErrorActionPreference = "Stop"

$ProjectDir = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
Set-Location $ProjectDir

if (-not $Version) {
    $match = Select-String -Path "build.gradle.kts" -Pattern 'version\s*=\s*"([^"]+)"' | Select-Object -First 1
    if (-not $match) { throw "No se pudo leer la version de build.gradle.kts" }
    $Version = $match.Matches[0].Groups[1].Value
}
Write-Host "==> Version: $Version"

Write-Host "==> 1. Compilando distribucion (Gradle installDist)..."
& .\gradlew.bat installDist --offline
if ($LASTEXITCODE -ne 0) { throw "Gradle installDist fallo" }

$BuildDir = Join-Path $ProjectDir "build\scoop"
if (Test-Path $BuildDir) { Remove-Item -Recurse -Force $BuildDir }
New-Item -ItemType Directory -Path $BuildDir | Out-Null

Write-Host "==> 2. Empaquetando con jpackage (runtime incluido)..."
$appImageDir = Join-Path $BuildDir "mm-generator"
& jpackage `
    --type app-image `
    --name "mm-generator" `
    --app-version $Version `
    --vendor "Buga" `
    --module-path "build\install\mm-generator\lib" `
    --module "com.buga.mmgenerator/com.buga.mmgenerator.HelloApplication" `
    --dest $BuildDir `
    --icon "packaging\common\com.buga.mmgenerator.ico" `
    --java-options "-Dfile.encoding=UTF-8"
if ($LASTEXITCODE -ne 0) { throw "jpackage fallo" }

$ZipName = "mm-generator-$Version-windows-x64.zip"
$ZipPath = Join-Path $BuildDir $ZipName
Write-Host "==> 3. Comprimiendo en $ZipName..."
if (Test-Path $ZipPath) { Remove-Item -Force $ZipPath }
Compress-Archive -Path $appImageDir -DestinationPath $ZipPath -Force

$Hash = (Get-FileHash $ZipPath -Algorithm SHA256).Hash.ToLower()
Write-Host ""
Write-Host "=========================================================="
Write-Host "  ZIP:    $ZipPath"
Write-Host "  SHA256: $Hash"
Write-Host "  TAMANO: $((Get-Item $ZipPath).Length) bytes"
Write-Host "=========================================================="

# Actualizar manifests
$ManifestUrl = "https://github.com/AlfonsoFichas-bit/mm-generator/releases/download/v$Version/$ZipName"
$AutoupdateUrl = 'https://github.com/AlfonsoFichas-bit/mm-generator/releases/download/v$version/mm-generator-$version-windows-x64.zip'

foreach ($rel in @("bucket\mm-generator.json", "packaging\scoop\mm-generator.json")) {
    $manifestPath = Join-Path $ProjectDir $rel
    if (-not (Test-Path $manifestPath)) { continue }
    $manifest = [ordered]@{
        version      = $Version
        description  = "Creador y editor de memes clasico y moderno"
        homepage     = "https://github.com/AlfonsoFichas-bit/mm-generator"
        license      = "GPL-3.0-or-later"
        architecture = [ordered]@{
            "64bit" = [ordered]@{
                url  = $ManifestUrl
                hash = "sha256:$Hash"
            }
        }
        extract_dir  = "mm-generator"
        bin          = "mm-generator.exe"
        shortcuts    = @(, @("mm-generator.exe", "MM Generator"))
        checkver     = [ordered]@{
            github = "https://github.com/AlfonsoFichas-bit/mm-generator"
        }
        autoupdate   = [ordered]@{
            architecture = [ordered]@{
                "64bit" = [ordered]@{
                    url = $AutoupdateUrl
                }
            }
        }
    }
    $json = $manifest | ConvertTo-Json -Depth 10
    Set-Content -Path $manifestPath -Value $json -Encoding UTF8
    Write-Host "Manifest actualizado: $rel"
}

Write-Host ""
Write-Host "Siguientes pasos:"
Write-Host "  1. Crear un GitHub Release con tag v$Version y subir $ZipName."
Write-Host "  2. Hacer commit de bucket/mm-generator.json y hacer push."
Write-Host "  3. En un PC con Scoop: scoop bucket add mm-generator https://github.com/AlfonsoFichas-bit/mm-generator"
Write-Host "     scoop install mm-generator/mm-generator"
