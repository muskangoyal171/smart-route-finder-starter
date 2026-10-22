# PowerShell launch script for Smart Route Finder JavaFX Desktop Application
$ErrorActionPreference = "Stop"

$jfxPath = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-base\21.0.8\javafx-base-21.0.8-win.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-graphics\21.0.8\javafx-graphics-21.0.8-win.jar;$env:USERPROFILE\.m2\repository\org\openjfx\javafx-controls\21.0.8\javafx-controls-21.0.8-win.jar"

Write-Host "Starting Smart Route Finder - VIT Vellore Campus..." -ForegroundColor Cyan
java --module-path $jfxPath --add-modules javafx.controls -cp "target/classes" com.smartroute.SmartRouteFinderGUI
