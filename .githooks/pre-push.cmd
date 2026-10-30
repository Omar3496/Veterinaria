@echo off
echo Running pre-push compile check on Veterinaria...
cd /d "%~dp0..\Veterinaria"
call mvnw.cmd test-compile
if errorlevel 1 (
    echo Pre-push compilation failed!
    exit /b 1
)
