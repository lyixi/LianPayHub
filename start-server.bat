@echo off
setlocal
cd /d "%~dp0"
if errorlevel 1 goto failed
title 懒一夕Official

echo Using the repository configuration. Optional environment variables can override it.
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=prod"
if not defined SPRING_DATASOURCE_URL set "SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/lianpayhub?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai"
if not defined SPRING_DATASOURCE_USERNAME set "SPRING_DATASOURCE_USERNAME=root"
if not defined SPRING_DATASOURCE_PASSWORD set "SPRING_DATASOURCE_PASSWORD=iwxyiroot"

where java >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Java was not found. Install JDK 8 or newer and add java to PATH.
    goto failed
)

where mvn >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Maven was not found. Install Maven 3.8+ and add mvn to PATH.
    echo [ERROR] This repository's mvnw.cmd cannot be used because its wrapper files are missing.
    goto failed
)

echo.
echo Starting the production server at http://localhost:8888/
echo Keep this window open while the server is running. Press Ctrl+C to stop it.
echo.
call mvn -B spring-boot:run
if errorlevel 1 goto failed
echo.
echo Server stopped.
pause
exit /b 0

:failed
echo.
echo Startup failed. Check the messages above, then press any key to close.
pause >nul
exit /b 1
