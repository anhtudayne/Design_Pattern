@echo off
TITLE Movie Booking Project - Smart Runner
Color 0A

echo =======================================================
echo    DANG KHOI CHAY DU AN DAT VE XEM PHIM (DESIGN PATTERN)
echo =======================================================

:: 1. Chay Docker
echo [1/3] Dang khoi dong Docker (MySQL, Redis)...
docker-compose up -d
if %errorlevel% neq 0 (
    echo [LOI] Khong the chay Docker Compose. Hay dam bao Docker Desktop dang bat!
    pause
    exit /b
)

:: 2. Chay Backend
echo [2/3] Dang mo cua so chay Backend...
start "Backend - Spring Boot" cmd /c "echo Dang chay Backend... && cd backend && mvnw.cmd spring-boot:run"

:: 3. Chay Frontend (Tu dong install neu thieu node_modules)
echo [3/3] Dang kiem tra va chay Frontend...
start "Frontend - React" cmd /c "cd frontend && if not exist node_modules (echo Thu vien dang thieu. Dang tien hanh npm install... && npm install) else (echo Thu vien da san sang.) && echo Dang chay Frontend... && npm run dev"

echo -------------------------------------------------------
echo TAT CA CAC DICH VU DANG DUOC KHOI CHAY:
echo - Backend: http://localhost:8080
echo - Frontend: http://localhost:5173
echo.
echo LUU Y: 
echo - Neu day la lan dau chay, cua so Frontend se ton vai phut de 'npm install'.
echo - Vui long khong tat cac cua so Terminal moi mo.
echo -------------------------------------------------------
pause
