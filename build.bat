@echo off
set JFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\21.0.8\javafx-base-21.0.8-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\21.0.8\javafx-graphics-21.0.8-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.8\javafx-controls-21.0.8-win.jar

if not exist target\classes mkdir target\classes
if not exist target\test-classes mkdir target\test-classes

echo Compiling Java sources...
powershell -ExecutionPolicy Bypass -File .\build.ps1

