@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
if errorlevel 1 goto failed
title 懒一夕Official

if not defined SPRING_DATASOURCE_URL (
    echo [ERROR] Set SPRING_DATASOURCE_URL to the production MySQL JDBC URL.
    goto failed
)
if not defined SPRING_DATASOURCE_USERNAME (
    echo [ERROR] Set SPRING_DATASOURCE_USERNAME.
    goto failed
)
if not defined SPRING_DATASOURCE_PASSWORD (
    echo [ERROR] Set SPRING_DATASOURCE_PASSWORD.
    goto failed
)
if not defined LIANPAYHUB_SECURITY_JWT_SECRET (
    echo [ERROR] Set LIANPAYHUB_SECURITY_JWT_SECRET to a strong random value.
    goto failed
)
if not defined LIANPAYHUB_ADMIN_DEFAULT_PASSWORD (
    echo [ERROR] Set LIANPAYHUB_ADMIN_DEFAULT_PASSWORD.
    goto failed
)
if not defined LIANPAYHUB_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS (
    echo [ERROR] Set LIANPAYHUB_SECURITY_CORS_ALLOWED_ORIGIN_PATTERNS to your HTTPS origin.
    goto failed
)

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

echo Building and testing the production server from the current source...
call mvn -B package
if errorlevel 1 goto failed

if not exist "target\lianpayhub-server-0.1.0-SNAPSHOT.jar" (
    echo [ERROR] The server JAR was not created.
    goto failed
)

echo.
echo Starting the production server at http://localhost:8888/
echo Keep this window open while the server is running. Press Ctrl+C to stop it.
echo.
java -jar "target\lianpayhub-server-0.1.0-SNAPSHOT.jar" --spring.profiles.active=prod
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
