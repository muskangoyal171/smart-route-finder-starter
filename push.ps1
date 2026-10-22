# PowerShell helper script to push Smart Route Finder to a new GitHub repository
param(
    [string]$RepoUrl
)

$ErrorActionPreference = "Stop"

# Locate git executable
$gitCmd = "git"
if (!(Get-Command git -ErrorAction SilentlyContinue)) {
    if (Test-Path "C:\Program Files\Git\cmd\git.exe") {
        $gitCmd = "C:\Program Files\Git\cmd\git.exe"
    } else {
        Write-Host "Error: Git executable not found. Please install Git or add it to PATH." -ForegroundColor Red
        exit 1
    }
}

Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "     SMART ROUTE FINDER - PUSH TO GITHUB        " -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan

# 1. Check user identity
$userName = & $gitCmd config user.name
$userEmail = & $gitCmd config user.email

if (-not $userName) {
    Write-Host "Git user.name is not set." -ForegroundColor Yellow
    $nameInput = Read-Host "Enter your GitHub/Git Name"
    if ($nameInput) {
        & $gitCmd config --global user.name $nameInput
    }
}

if (-not $userEmail) {
    Write-Host "Git user.email is not set." -ForegroundColor Yellow
    $emailInput = Read-Host "Enter your GitHub/Git Email"
    if ($emailInput) {
        & $gitCmd config --global user.email $emailInput
    }
}

# 2. Check repository URL
if (-not $RepoUrl) {
    Write-Host "`nPlease create a new empty repository on GitHub (https://github.com/new)." -ForegroundColor Green
    Write-Host "(Do NOT check 'Add README', 'Add .gitignore', or 'License' on GitHub)`n" -ForegroundColor Yellow
    $RepoUrl = Read-Host "Paste your GitHub Repository URL (e.g. https://github.com/username/my-repo.git)"
}

if (-not $RepoUrl) {
    Write-Host "Error: Repository URL is required to push." -ForegroundColor Red
    exit 1
}

# 3. Initialize repository if not already done
if (!(Test-Path ".git")) {
    Write-Host "Initializing local Git repository..." -ForegroundColor Cyan
    & $gitCmd init -b main
} else {
    & $gitCmd branch -M main
}

# 4. Stage and commit
Write-Host "Staging files..." -ForegroundColor Cyan
& $gitCmd add .

Write-Host "Creating initial commit..." -ForegroundColor Cyan
& $gitCmd commit -m "Initial commit: Smart Route Finder - VIT Vellore Campus (PAMCA501 DSA)"

# 5. Set remote
Write-Host "Connecting remote origin to: $RepoUrl" -ForegroundColor Cyan
try {
    & $gitCmd remote remove origin 2>$null
} catch {}
& $gitCmd remote add origin $RepoUrl

# 6. Push to GitHub
Write-Host "Pushing to GitHub (main branch)..." -ForegroundColor Cyan
Write-Host "If prompted, sign in via your browser with Git Credential Manager." -ForegroundColor Yellow
& $gitCmd push -u origin main

Write-Host "`nCode pushed to GitHub successfully!" -ForegroundColor Green

