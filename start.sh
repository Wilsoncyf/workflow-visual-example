#!/bin/bash

echo "=== 可视化工作流编排系统启动脚本 ==="

# 检查Java环境
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java环境，请安装Java 17或更高版本"
    exit 1
fi

# 检查Node.js环境
if ! command -v npm &> /dev/null; then
    echo "错误: 未找到npm，请安装Node.js"
    exit 1
fi

# 检查MySQL
echo "请确保MySQL服务正在运行 (localhost:3306, root/root)"
echo "按任意键继续..."
read -n 1 -s

# 构建后端
echo "正在构建后端项目..."
mvn clean package -DskipTests
if [ $? -ne 0 ]; then
    echo "后端构建失败"
    exit 1
fi

# 启动后端服务
echo "正在启动后端服务..."
nohup java -jar target/workflow-visual-example-1.0.0.jar > backend.log 2>&1 &
BACKEND_PID=$!
echo "后端服务已启动，PID: $BACKEND_PID"
echo "后端日志: tail -f backend.log"

# 等待后端启动
echo "等待后端服务启动..."
sleep 10

# 检查后端是否启动成功
if ! curl -s http://localhost:8080/api/workflows > /dev/null; then
    echo "后端服务启动失败，请检查 backend.log"
    kill $BACKEND_PID
    exit 1
fi
echo "后端服务启动成功 (http://localhost:8080)"

# 安装前端依赖
echo "正在安装前端依赖..."
cd workflow-frontend
if [ ! -d "node_modules" ]; then
    npm install
    if [ $? -ne 0 ]; then
        echo "前端依赖安装失败"
        kill $BACKEND_PID
        exit 1
    fi
fi

# 启动前端开发服务器
echo "正在启动前端服务..."
nohup npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
cd ..

echo ""
echo "=== 启动完成 ==="
echo "后端服务: http://localhost:8080"
echo "前端应用: http://localhost:5173"
echo "后端PID: $BACKEND_PID"
echo "前端PID: $FRONTEND_PID"
echo ""
echo "查看日志:"
echo "  后端: tail -f backend.log"
echo "  前端: tail -f frontend.log"
echo ""
echo "停止服务:"
echo "  kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "请访问 http://localhost:5173 开始使用工作流编辑器"

# 保存PID到文件
echo $BACKEND_PID > backend.pid
echo $FRONTEND_PID > frontend.pid