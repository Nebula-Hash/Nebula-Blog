@echo off
setlocal enabledelayedexpansion

echo ==========================================
echo Nebula Blog - 本地构建和测试脚本
echo ==========================================
echo.

REM 检查 Docker
where docker >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: Docker 未安装或未在 PATH 中
    exit /b 1
)

REM 检查 Maven
where mvn >nul 2>nul
if %errorlevel% neq 0 (
    echo 错误: Maven 未安装或未在 PATH 中
    exit /b 1
)

echo 所有必要工具已安装
echo.

REM 清理旧的容器和镜像
echo ==========================================
echo 步骤 1: 清理旧的容器和镜像
echo ==========================================
cd docker-deployment

echo 停止并删除旧容器...
docker-compose --env-file .env.local-test down -v 2>nul

cd ..

echo 删除旧镜像...
docker rmi nebula-blog-backend:latest 2>nul
docker rmi nebula-blog-frontend-gateway:latest 2>nul
docker rmi nebula-blog-web:latest 2>nul
docker rmi nebula-blog-admin:latest 2>nul

echo 清理完成
echo.

REM 构建后端
echo ==========================================
echo 步骤 2: 构建后端服务
echo ==========================================
cd Spring-server

echo Maven 打包...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 错误: Maven 打包失败
    exit /b 1
)

echo 构建 Docker 镜像...
docker build -t nebula-blog-backend:latest .
if %errorlevel% neq 0 (
    echo 错误: 后端镜像构建失败
    exit /b 1
)

cd ..
echo 后端构建完成
echo.

REM 构建前端 - Blog Web
echo ==========================================
echo 步骤 3: 构建 Blog Web
echo ==========================================
cd blog-web

echo 构建 Docker 镜像...
docker build -t nebula-blog-web:latest .
if %errorlevel% neq 0 (
    echo 错误: Blog Web 镜像构建失败
    exit /b 1
)

cd ..
echo Blog Web 构建完成
echo.

REM 构建前端 - Admin Web
echo ==========================================
echo 步骤 4: 构建 Admin Web
echo ==========================================
cd admin-web

echo 构建 Docker 镜像...
docker build -t nebula-blog-admin:latest .
if %errorlevel% neq 0 (
    echo 错误: Admin Web 镜像构建失败
    exit /b 1
)

cd ..
echo Admin Web 构建完成
echo.

REM 构建 Gateway
echo ==========================================
echo 步骤 5: 构建 Gateway
echo ==========================================
cd docker-deployment

echo 构建 Docker 镜像...
docker build -t nebula-blog-frontend-gateway:latest -f gateway/Dockerfile .
if %errorlevel% neq 0 (
    echo 错误: Gateway 镜像构建失败
    exit /b 1
)

cd ..
echo Gateway 构建完成
echo.

REM 显示镜像信息
echo ==========================================
echo 构建的镜像列表
echo ==========================================
docker images | findstr "nebula-blog"
echo.

REM 启动容器
echo ==========================================
echo 步骤 6: 启动所有容器
echo ==========================================
cd docker-deployment

echo 使用 docker-compose 启动服务...
docker-compose --env-file .env.local-test up -d
if %errorlevel% neq 0 (
    echo 错误: 容器启动失败
    exit /b 1
)

echo.
echo 等待容器启动...
timeout /t 5 /nobreak >nul

echo.
echo ==========================================
echo 容器状态
echo ==========================================
docker-compose --env-file .env.local-test ps

echo.
echo ==========================================
echo 步骤 7: 健康检查
echo ==========================================

echo 等待服务完全启动 (30秒)...
timeout /t 30 /nobreak >nul

REM 检查后端健康
echo.
echo 检查后端服务...
curl -f -s http://localhost:8081/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo 后端服务正常
) else (
    echo 后端服务可能还在启动中
)

REM 检查 Blog Web
echo 检查 Blog Web...
curl -f -s http://localhost:3000/healthz >nul 2>&1
if %errorlevel% equ 0 (
    echo Blog Web 正常
) else (
    echo Blog Web 可能还在启动中
)

REM 检查 Admin Web
echo 检查 Admin Web...
curl -f -s http://localhost:3001/healthz >nul 2>&1
if %errorlevel% equ 0 (
    echo Admin Web 正常
) else (
    echo Admin Web 可能还在启动中
)

echo.
echo ==========================================
echo 构建和启动完成！
echo ==========================================
echo.
echo 访问地址：
echo   - Blog Web:  http://localhost:3000
echo   - Admin Web: http://localhost:3001
echo   - Backend:   http://localhost:8081
echo.
echo 查看日志：
echo   docker-compose --env-file .env.local-test logs -f [service-name]
echo.
echo 服务名称：
echo   - backend
echo   - frontend-gateway
echo   - blog-web
echo   - admin-web
echo.
echo 停止服务：
echo   cd docker-deployment ^&^& docker-compose --env-file .env.local-test down
echo.

cd ..
