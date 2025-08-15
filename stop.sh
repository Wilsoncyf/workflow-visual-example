#!/bin/bash

echo "=== 停止可视化工作流编排系统 ==="

# 读取PID文件并停止服务
if [ -f "backend.pid" ]; then
    BACKEND_PID=$(cat backend.pid)
    if ps -p $BACKEND_PID > /dev/null; then
        echo "正在停止后端服务 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        echo "后端服务已停止"
    else
        echo "后端服务已经停止"
    fi
    rm -f backend.pid
else
    echo "未找到后端PID文件"
fi

if [ -f "frontend.pid" ]; then
    FRONTEND_PID=$(cat frontend.pid)
    if ps -p $FRONTEND_PID > /dev/null; then
        echo "正在停止前端服务 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        echo "前端服务已停止"
    else
        echo "前端服务已经停止"
    fi
    rm -f frontend.pid
else
    echo "未找到前端PID文件"
fi

# 清理日志文件
echo "清理日志文件..."
rm -f backend.log frontend.log

echo "=== 所有服务已停止 ==="