# `push.ps1` — File Guide

## Purpose

`push.ps1` is an interactive PowerShell script that automates pushing the Smart Route Finder codebase to a new GitHub repository. It checks Git availability, guides the user through setting their Git identity (`user.name`, `user.email`), prompts for the GitHub repository URL, stages all tracked files, creates the initial commit on `main`, connects the remote origin, and pushes to GitHub using the Git Credential Manager.

## Usage

```powershell
.\push.ps1
```
Or passing the repository URL directly:
```powershell
.\push.ps1 -RepoUrl "https://github.com/<username>/<repo>.git"
```

