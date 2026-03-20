@echo off
REM =====================================================
REM SCampus Forum System - One-Click Start Script
REM This script compiles and starts all backend services
REM and frontend application
REM =====================================================

echo.
echo =====================================================
echo   SCampus Forum System - One-Click Start Script
echo =====================================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven first
    pause
    exit /b 1
)

REM Check if Node.js is installed
where node >nul 2>nul
if %errorlevel% neq 0 (
    echo [ERROR] Node.js is not installed or not in PATH
    echo Please install Node.js first
    pause
    exit /b 1
)

REM Get current directory
set PROJECT_ROOT=%~dp0
cd /d %PROJECT_ROOT%

echo [Step 1/4] Compiling backend project...
echo.
call mvn clean install -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] Backend compilation failed
    pause
    exit /b 1
)
echo [SUCCESS] Backend compilation completed
echo.

echo [Step 2/4] Starting backend services...
echo.
echo Starting services in new windows...
echo.

REM Start Gateway Service (Must be first)
echo Starting Gateway Service on port 8080...
start "SCampus-Gateway" cmd /k "cd /d %PROJECT_ROOT%forum-gateway && mvn spring-boot:run"

REM Wait for gateway to start
timeout /t 10 /nobreak >nul

REM Start Auth Service
echo Starting Auth Service on port 9001...
start "SCampus-Auth" cmd /k "cd /d %PROJECT_ROOT%forum-auth && mvn spring-boot:run"

REM Start User Service
echo Starting User Service on port 9002...
start "SCampus-User" cmd /k "cd /d %PROJECT_ROOT%forum-user && mvn spring-boot:run"

REM Start Category Service
echo Starting Category Service on port 9003...
start "SCampus-Category" cmd /k "cd /d %PROJECT_ROOT%forum-category && mvn spring-boot:run"

REM Start Post Service
echo Starting Post Service on port 9004...
start "SCampus-Post" cmd /k "cd /d %PROJECT_ROOT%forum-post && mvn spring-boot:run"

REM Start Comment Service
echo Starting Comment Service on port 9005...
start "SCampus-Comment" cmd /k "cd /d %PROJECT_ROOT%forum-comment && mvn spring-boot:run"

REM Start Interaction Service
echo Starting Interaction Service on port 9006...
start "SCampus-Interaction" cmd /k "cd /d %PROJECT_ROOT%forum-interaction && mvn spring-boot:run"

REM Start Report Service
echo Starting Report Service on port 9007...
start "SCampus-Report" cmd /k "cd /d %PROJECT_ROOT%forum-report && mvn spring-boot:run"

REM Start Stats Service
echo Starting Stats Service on port 9008...
start "SCampus-Stats" cmd /k "cd /d %PROJECT_ROOT%forum-stats && mvn spring-boot:run"

REM Start Notify Service
echo Starting Notify Service on port 9009...
start "SCampus-Notify" cmd /k "cd /d %PROJECT_ROOT%forum-notify && mvn spring-boot:run"

REM Start File Service
echo Starting File Service on port 9010...
start "SCampus-File" cmd /k "cd /d %PROJECT_ROOT%forum-file && mvn spring-boot:run"

echo.
echo [SUCCESS] All backend services are starting...
echo Please wait for all services to fully start (about 30-60 seconds)
echo.

echo [Step 3/4] Installing frontend dependencies...
echo.
cd /d %PROJECT_ROOT%forum-web
call npm install
if %errorlevel% neq 0 (
    echo [WARNING] Frontend dependency installation had issues, continuing...
)
echo.

echo [Step 4/4] Starting frontend application...
echo.
echo Starting Frontend on port 3000...
start "SCampus-Frontend" cmd /k "cd /d %PROJECT_ROOT%forum-web && npm run dev"

echo.
echo =====================================================
echo   All services have been started!
echo =====================================================
echo.
echo   Backend Services:
echo     - Gateway:    http://localhost:8080
echo     - Auth:       http://localhost:9001
echo     - User:       http://localhost:9002
echo     - Category:   http://localhost:9003
echo     - Post:       http://localhost:9004
echo     - Comment:    http://localhost:9005
echo     - Interaction:http://localhost:9006
echo     - Report:     http://localhost:9007
echo     - Stats:      http://localhost:9008
echo     - Notify:     http://localhost:9009
echo     - File:       http://localhost:9010
echo.
echo   Frontend:
echo     - Web UI:     http://localhost:3000
echo.
echo   API Documentation:
echo     - Gateway:    http://localhost:8080/doc.html
echo.
echo   Prerequisites:
echo     - MySQL running on port 3306
echo     - Redis running on port 6379
echo     - Nacos running on port 8848
echo.
echo =====================================================
echo.

REM Return to project root
cd /d %PROJECT_ROOT%

echo Press any key to close this window...
echo (Backend and Frontend windows will remain open)
pause >nul
