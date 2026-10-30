# scripts/check-coding-standards.ps1
# Mechanical checker for CODING_STANDARDS.md rules

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
$srcMain = Join-Path $repoRoot "Veterinaria\src\main\java"

$violations = 0

Write-Host "Running coding standards mechanical checks..." -ForegroundColor Cyan

# Rule 1: In-memory concurrency stores must carry // shortcut: comment
$javaFiles = Get-ChildItem -Path $srcMain -Recurse -Filter "*.java"

foreach ($file in $javaFiles) {
    $lines = Get-Content $file.FullName
    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -match "new\s+ConcurrentHashMap" -or $line -match "Collections\.synchronized") {
            $hasComment = $false
            if ($line -match "shortcut:") {
                $hasComment = $true
            } elseif ($i -gt 0 -and $lines[$i - 1] -match "shortcut:") {
                $hasComment = $true
            } elseif ($i -gt 1 -and $lines[$i - 2] -match "shortcut:") {
                $hasComment = $true
            }
            if (-not $hasComment) {
                Write-Host "❌ [Rule 42 Violation] $($file.Name):$($i + 1) In-memory concurrency store without // shortcut: comment." -ForegroundColor Red
                $violations++
            }
        }
    }
}

# Rule 2: Controllers must not define private extractUsername or extractRole (use SecurityUtils)
$controllerFiles = Get-ChildItem -Path (Join-Path $srcMain "com\tecsolutions\Veterinaria\controller") -Filter "*Controller.java"

foreach ($file in $controllerFiles) {
    $content = Get-Content $file.FullName -Raw
    if ($content -match "private\s+String\s+extractUsername\s*\(") {
        Write-Host "❌ [Duplication Smell] $($file.Name): Contains private extractUsername method. Use SecurityUtils instead." -ForegroundColor Red
        $violations++
    }
    if ($content -match "private\s+String\s+extractRole\s*\(") {
        Write-Host "❌ [Duplication Smell] $($file.Name): Contains private extractRole method. Use SecurityUtils instead." -ForegroundColor Red
        $violations++
    }
}

if ($violations -eq 0) {
    Write-Host "✅ All mechanical coding standard checks passed cleanly!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "❌ Found $violations coding standard violation(s)." -ForegroundColor Red
    exit 1
}
