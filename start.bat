@echo off
REM =====================================================
REM SCampus Forum System - One-Click Start Script
REM This script compiles and starts all backend services
REM and frontend application
REM 
REM Prerequisites:
REM - JDK 1.8+
REM - Maven 3.6+
REM - Node.js 18+
REM - MySQL 8.0 (running on port 3306)
REM - Redis (running on port 6379)
REM =====================================================

echo.
echo =====================================================
echo   SCampus Forum System - One-Click Start Script
echo =====================================================
echo.
echo Prerequisites:
echo   - MySQL running on port 3306
echo   - Redis running on port 6379
echo   - JDK 1.8+ installed
echo   - Maven 3.6+ installed
echo   - Node.js 18+ installed
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
    echo Please check the error messages above
    pause
    exit /b 1
)
echo [SUCCESS] Backend compilation completed
echo.

echo [Step 2/4] Starting backend services...
echo.
echo Starting services in new windows...
echo.

REM Start Auth Service (Port 9001)
echo Starting Auth Service on port 9001...
start "SCampus-Auth" cmd /k "cd /d %PROJECT_ROOT%forum-auth && mvn spring-boot:run"

REM Wait for auth to start
timeout /t 5 /nobreak >nul

REM Start User Service (Port 9002)
echo Starting User Service on port 9002...
start "SCampus-User" cmd /k "cd /d %PROJECT_ROOT%forum-user && mvn spring-boot:run"

REM Start Category Service (Port 9003)
echo Starting Category Service on port 9003...
start "SCampus-Category" cmd /k "cd /d %PROJECT_ROOT%forum-category && mvn spring-boot:run"

REM Start Post Service (Port 9004)
echo Starting Post Service on port 9004...
start "SCampus-Post" cmd /k "cd /d %PROJECT_ROOT%forum-post && mvn spring-boot:run"

REM Start Comment Service (Port 9005)
echo Starting Comment Service on port 9005...
start "SCampus-Comment" cmd /k "cd /d %PROJECT_ROOT%forum-comment && mvn spring-boot:run"

REM Start Interaction Service (Port 9006)
echo Starting Interaction Service on port 9006...
start "SCampus-Interaction" cmd /k "cd /d %PROJECT_ROOT%forum-interaction && mvn spring-boot:run"

REM Start Report Service (Port 9007)
echo Starting Report Service on port 9007...
start "SCampus-Report" cmd /k "cd /d %PROJECT_ROOT%forum-report && mvn spring-boot:run"

REM Start Stats Service (Port 9008)
echo Starting Stats Service on port 9008...
start "SCampus-Stats" cmd /k "cd /d %PROJECT_ROOT%forum-stats && mvn spring-boot:run"

REM Start Notify Service (Port 9009)
echo Starting Notify Service on port 9009...
start "SCampus-Notify" cmd /k "cd /d %PROJECT_ROOT%forum-notify && mvn spring-boot:run"

REM Start File Service (Port 9010)
echo Starting File Service on port 9010...
start "SCampus-File" cmd /k "cd /d %PROJECT_ROOT%forum-file && mvn spring-boot:run"

REM Wait for services to start
timeout /t 10 /nobreak >nul

REM Start Gateway Service (Port 8080) - Must be last
echo Starting Gateway Service on port 8080...
start "SCampus-Gateway" cmd /k "cd /d %PROJECT_ROOT%forum-gateway && mvn spring-boot:run"

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
echo   Backend Services (Start in order):
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
echo     - Gateway:    http://localhost:8080
echo.
echo   Frontend:
echo     - Web UI:     http://localhost:3000
echo.
echo   API Documentation (Swagger):
echo     - Auth:       http://localhost:9001/doc.html
echo     - User:       http://localhost:9002/doc.html
echo     - Gateway:    http://localhost:8080/doc.html
echo.
echo   Database Requirements:
echo     - MySQL running on port 3306
echo     - Redis running on port 6379
echo.
echo =====================================================
echo.

REM Return to project root
cd /d %PROJECT_ROOT%

echo Press any key to close this window...
echo (Backend and Frontend windows will remain open)
pause >nul
