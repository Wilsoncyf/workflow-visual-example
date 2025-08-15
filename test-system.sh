#!/bin/bash

echo "=== 可视化工作流编排系统测试脚本 ==="

# 检查MySQL连接
echo "1. 检查MySQL连接..."
if command -v mysql &> /dev/null; then
    mysql -h localhost -P 3306 -u root -proot -e "SELECT 1;" 2>/dev/null
    if [ $? -eq 0 ]; then
        echo "✅ MySQL连接成功"
    else
        echo "❌ MySQL连接失败，请检查服务是否启动，用户名密码是否正确"
        exit 1
    fi
else
    echo "⚠️  未找到mysql命令，跳过连接测试"
fi

# 启动后端
echo "2. 启动后端服务..."
cd /Users/chenyongfeng/Documents/project/spring-ai-alibaba/spring-ai-alibaba-graph/workflow-visual-example

java -jar target/workflow-visual-example-1.0.0.jar > backend.log 2>&1 &
BACKEND_PID=$!
echo "后端PID: $BACKEND_PID"

# 等待后端启动
echo "3. 等待后端启动..."
for i in {1..30}; do
    if curl -s http://localhost:8080/api/workflows > /dev/null 2>&1; then
        echo "✅ 后端服务启动成功"
        break
    fi
    echo "等待中... ($i/30)"
    sleep 2
done

# 测试后端API
echo "4. 测试后端API..."
echo "GET /api/workflows:"
curl -s http://localhost:8080/api/workflows | jq . 2>/dev/null || echo "JSON解析失败，原始响应："
curl -s http://localhost:8080/api/workflows

echo ""
echo "5. 启动前端..."
cd workflow-frontend

# 检查依赖
if [ ! -d "node_modules" ]; then
    echo "安装前端依赖..."
    npm install
fi

# 启动前端
npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "前端PID: $FRONTEND_PID"

echo ""
echo "=== 系统启动完成 ==="
echo "后端: http://localhost:8080 (PID: $BACKEND_PID)"
echo "前端: http://localhost:5173 (PID: $FRONTEND_PID)"
echo ""
echo "日志文件:"
echo "  后端: tail -f backend.log"
echo "  前端: tail -f frontend.log"
echo ""
echo "停止服务: kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "请在浏览器中访问: http://localhost:5173"
echo "按 Ctrl+C 停止监控，服务将继续运行"

# 保存PID
echo $BACKEND_PID > backend.pid
echo $FRONTEND_PID > frontend.pid

# 监控日志
trap "echo '停止监控，服务仍在运行'; exit 0" INT
echo ""
echo "=== 实时日志 (Ctrl+C 退出监控) ==="
tail -f backend.log frontend.log