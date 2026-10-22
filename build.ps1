# PowerShell build script for Smart Route Finder
$ErrorActionPreference = "Stop"

$jfxPath = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-base\21.0.8\javafx-base-21.0.8-win.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-graphics\21.0.8\javafx-graphics-21.0.8-win.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-controls\21.0.8\javafx-controls-21.0.8-win.jar"

if (!(Test-Path "target/classes")) { New-Item -ItemType Directory -Path "target/classes" -Force | Out-Null }
if (!(Test-Path "target/test-classes")) { New-Item -ItemType Directory -Path "target/test-classes" -Force | Out-Null }

Write-Host "Compiling main sources..." -ForegroundColor Cyan
$mainFiles = Get-ChildItem -Recurse -Path "src/main/java" -Filter "*.java" | Select-Object -ExpandProperty FullName
javac --module-path $jfxPath --add-modules javafx.controls -d target/classes $mainFiles

Write-Host "Copying resources..." -ForegroundColor Cyan
Copy-Item -Recurse -Path "src/main/resources/*" -Destination "target/classes" -Force

Write-Host "Compiling test sources..." -ForegroundColor Cyan
$testFiles = Get-ChildItem -Recurse -Path "src/test/java" -Filter "*.java" | Select-Object -ExpandProperty FullName
javac --module-path $jfxPath --add-modules javafx.controls -cp "target/classes" -d target/test-classes $testFiles

Write-Host "Build completed successfully!" -ForegroundColor Green

