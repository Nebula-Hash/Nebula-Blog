#!/bin/bash

set -e

echo "=========================================="
echo "Nebula Blog - 本地构建和测试脚本"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查必要的命令
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo -e "${RED}错误: $1 未安装${NC}"
        exit 1
    fi
}

echo "检查必要的工具..."
check_command docker
check_command docker-compose
check_command mvn
echo -e "${GREEN}✓ 所有必要工具已安装${NC}"
echo ""

# 清理旧的容器和镜像
echo "=========================================="
echo "步骤 1: 清理旧的容器和镜像"
echo "=========================================="
cd docker-deployment

echo "停止并删除旧容器..."
docker-compose --env-file .env.local-test down -v 2>/dev/null || true

cd ..

echo "删除旧镜像..."
docker rmi nebula-blog-backend:latest 2>/dev/null || true
docker rmi nebula-blog-frontend-gateway:latest 2>/dev/null || true
docker rmi nebula-blog-web:latest 2>/dev/null || true
docker rmi nebula-blog-admin:latest 2>/dev/null || true

echo -e "${GREEN}✓ 清理完成${NC}"
echo ""

# 构建后端
echo "=========================================="
echo "步骤 2: 构建后端服务"
echo "=========================================="
cd Spring-server

echo "Maven 打包..."
mvn clean package -DskipTests

echo "构建 Docker 镜像..."
docker build -t nebula-blog-backend:latest .

cd ..
echo -e "${GREEN}✓ 后端构建完成${NC}"
echo ""

# 构建前端 - Blog Web
echo "=========================================="
echo "步骤 3: 构建 Blog Web"
echo "=========================================="
cd blog-web

echo "构建 Docker 镜像..."
docker build -t nebula-blog-web:latest .

cd ..
echo -e "${GREEN}✓ Blog Web 构建完成${NC}"
echo ""

# 构建前端 - Admin Web
echo "=========================================="
echo "步骤 4: 构建 Admin Web"
echo "=========================================="
cd admin-web

echo "构建 Docker 镜像..."
docker build -t nebula-blog-admin:latest .

cd ..
echo -e "${GREEN}✓ Admin Web 构建完成${NC}"
echo ""

# 构建 Gateway
echo "=========================================="
echo "步骤 5: 构建 Gateway"
echo "=========================================="
cd docker-deployment

echo "构建 Docker 镜像..."
docker build -t nebula-blog-frontend-gateway:latest -f gateway/Dockerfile .

cd ..
echo -e "${GREEN}✓ Gateway 构建完成${NC}"
echo ""

# 显示镜像信息
echo "=========================================="
echo "构建的镜像列表"
echo "=========================================="
docker images | grep -E "REPOSITORY|nebula-blog"
echo ""

# 启动容器
echo "=========================================="
echo "步骤 6: 启动所有容器"
echo "=========================================="
cd docker-deployment

echo "使用 docker-compose 启动服务..."
docker-compose --env-file .env.local-test up -d

echo ""
echo "等待容器启动..."
sleep 5

echo ""
echo "=========================================="
echo "容器状态"
echo "=========================================="
docker-compose --env-file .env.local-test ps

echo ""
echo "=========================================="
echo "步骤 7: 健康检查"
echo "=========================================="

# 等待服务启动
echo "等待服务完全启动 (30秒)..."
for i in {1..30}; do
    echo -n "."
    sleep 1
done
echo ""

# 检查后端健康
echo ""
echo "检查后端服务..."
if curl -f -s http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✓ 后端服务正常${NC}"
else
    echo -e "${YELLOW}⚠ 后端服务可能还在启动中${NC}"
fi

# 检查 Blog Web
echo "检查 Blog Web..."
if curl -f -s http://localhost:3000/healthz > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Blog Web 正常${NC}"
else
    echo -e "${YELLOW}⚠ Blog Web 可能还在启动中${NC}"
fi

# 检查 Admin Web
echo "检查 Admin Web..."
if curl -f -s http://localhost:3001/healthz > /dev/null 2>&1; then
    echo -e "${GREEN}✓ Admin Web 正常${NC}"
else
    echo -e "${YELLOW}⚠ Admin Web 可能还在启动中${NC}"
fi

echo ""
echo "=========================================="
echo "构建和启动完成！"
echo "=========================================="
echo ""
echo "访问地址："
echo "  - Blog Web:  http://localhost:3000"
echo "  - Admin Web: http://localhost:3001"
echo "  - Backend:   http://localhost:8081"
echo ""
echo "查看日志："
echo "  docker-compose --env-file .env.local-test logs -f [service-name]"
echo ""
echo "服务名称："
echo "  - backend"
echo "  - frontend-gateway"
echo "  - blog-web"
echo "  - admin-web"
echo ""
echo "停止服务："
echo "  cd docker-deployment && docker-compose --env-file .env.local-test down"
echo ""
