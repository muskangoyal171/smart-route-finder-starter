@echo off
setlocal
echo Starting push to GitHub...
powershell -ExecutionPolicy Bypass -File .\push.ps1
pause

