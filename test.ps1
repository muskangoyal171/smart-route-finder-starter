# PowerShell test runner script for Smart Route Finder Automated Test Suites
$ErrorActionPreference = "Stop"

$jfxPath = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-base\21.0.8\javafx-base-21.0.8-win.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-graphics\21.0.8\javafx-graphics-21.0.8-win.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-controls\21.0.8\javafx-controls-21.0.8-win.jar"

Write-Host "Running all 7 automated test suites via AllTestsRunner..." -ForegroundColor Cyan
java --module-path $jfxPath --add-modules javafx.controls -cp "target/classes;target/test-classes" com.smartroute.AllTestsRunner

