# 可视化工作流编排系统 - 快速启动指南

## 🎯 系统功能

✅ **拖拽式工作流设计器** - 基于 Vue Flow 的可视化编辑器  
✅ **4种内置节点类型** - 输入、处理、延迟、输出节点  
✅ **完整的工作流管理** - 新建、保存、加载、删除工作流  
✅ **实时执行监控** - 异步执行工作流并实时更新状态  
✅ **MySQL 数据持久化** - 工作流定义和执行历史存储  
✅ **中文友好界面** - 完整的中文用户界面

## 🚀 环境要求

- **Java**: 17 或更高版本
- **Node.js**: 16 或更高版本
- **MySQL**: 8.0 运行在 localhost:3306
  - 用户名: `root`
  - 密码: `root`
  - 系统会自动创建 `workflow_db` 数据库

## ⚡ 快速启动

### 方法1: 一键启动脚本

```bash
# 克隆或进入项目目录
cd /Users/chenyongfeng/Documents/project/spring-ai-alibaba/spring-ai-alibaba-graph/workflow-visual-example

# 确保 MySQL 服务已启动，然后运行：
./start.sh
```

### 方法2: 手动启动

#### 启动后端服务
```bash
cd /Users/chenyongfeng/Documents/project/spring-ai-alibaba/spring-ai-alibaba-graph/workflow-visual-example

# 构建项目
mvn clean package -DskipTests

# 启动后端服务
java -jar target/workflow-visual-example-1.0.0.jar
```

#### 启动前端服务 (新终端窗口)
```bash
cd workflow-frontend

# 安装依赖 (仅首次)
npm install

# 启动开发服务器
npm run dev
```

## 🌐 访问地址

- **前端应用**: http://localhost:5173
- **后端API**: http://localhost:8080  
- **API测试**: http://localhost:8080/api/workflows

## 🎮 使用教程

### 1. 创建第一个工作流

1. 打开浏览器访问 http://localhost:5173
2. 点击顶部的 **"新建"** 按钮
3. 输入工作流名称，如 "我的第一个工作流"
4. 系统会显示默认的三节点工作流

### 2. 编辑工作流

1. **拖拽添加节点**: 从左侧节点库拖拽节点到画布
   - 输入节点 (绿色): 工作流起点
   - 处理节点 (蓝色): 数据处理逻辑  
   - 延迟节点 (橙色): 模拟耗时操作
   - 输出节点 (红色): 工作流终点

2. **连接节点**: 拖拽节点边缘的连接点来创建连线

3. **编辑属性**: 点击节点，在右侧属性面板修改节点标签

### 3. 保存和执行

1. 点击 **"保存"** 按钮保存工作流
2. 点击 **"执行"** 按钮运行工作流
3. 在左侧执行历史面板查看执行状态和结果

### 4. 管理工作流

- **加载工作流**: 在顶部下拉菜单选择已保存的工作流，点击"加载"
- **删除工作流**: 选择要删除的工作流，点击"删除"按钮

## 🛠️ 节点类型说明

### 输入节点
- 作用: 工作流的入口点
- 输出: 包含初始输入数据

### 处理节点
- 作用: 执行数据处理逻辑
- 功能: 将输入数据加工处理
- 输出: 处理后的数据和时间戳

### 延迟节点
- 作用: 模拟耗时操作
- 功能: 等待1秒钟
- 用途: 测试长时间运行的任务

### 输出节点
- 作用: 工作流的输出点
- 功能: 汇总所有中间结果
- 输出: 最终执行结果

## 📊 数据库结构

系统会自动在 MySQL 中创建以下表：

### `workflows` 表
```sql
CREATE TABLE workflows (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    graph_data TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'draft',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### `executions` 表
```sql  
CREATE TABLE executions (
    id VARCHAR(255) PRIMARY KEY,
    workflow_id VARCHAR(255) NOT NULL,
    inputs TEXT,
    outputs TEXT,
    status VARCHAR(50) NOT NULL,
    error_message TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    duration BIGINT
);
```

## 🔧 故障排除

### 常见问题

#### 1. 后端启动失败
```
错误: 找不到或无法加载主类
```
**解决**: 确保已执行 `mvn clean package -DskipTests` 构建项目

#### 2. 数据库连接失败
```
java.sql.SQLException: Access denied for user 'root'@'localhost'
```
**解决**: 
- 确认 MySQL 服务已启动
- 检查用户名密码是否为 `root/root`
- 确保 MySQL 端口为 `3306`

#### 3. 前端无法连接后端
```
Network Error / CORS Error
```
**解决**:
- 确认后端服务运行在 8080 端口
- 检查防火墙设置
- 确认后端服务已完全启动

#### 4. 工作流执行失败
**解决**:
- 检查后端日志: `tail -f backend.log`
- 确认工作流结构正确
- 检查数据库连接状态

### 查看日志

```bash
# 查看后端日志
tail -f backend.log

# 查看前端日志  
tail -f frontend.log

# 查看实时执行状态
curl http://localhost:8080/api/workflows
```

### 停止服务

```bash
# 使用停止脚本
./stop.sh

# 或手动停止
kill $(cat backend.pid frontend.pid)
```

## 🔄 开发模式

如需修改代码并实时查看效果：

### 后端开发
```bash
mvn spring-boot:run
# 代码修改后会自动重启
```

### 前端开发
```bash
cd workflow-frontend
npm run dev
# 保存文件会自动热重载
```

## 🎯 下一步

系统现在已经完全可用！你可以：

1. 🎨 **创建复杂工作流** - 尝试连接更多节点
2. 📊 **监控执行过程** - 观察不同节点的执行状态
3. 🔧 **扩展节点类型** - 在后端添加自定义节点
4. 🌐 **集成外部服务** - 通过 HTTP 节点调用API
5. 📈 **性能测试** - 创建大型工作流测试系统性能

## 💡 技术架构

- **前端**: Vue 3 + TypeScript + Vue Flow + Element Plus
- **后端**: Spring Boot + Graph Core + JPA + MySQL
- **实时通信**: HTTP 轮询 (可扩展为 WebSocket)
- **数据存储**: MySQL 8.0

## 📝 API 文档

### 工作流管理
- `GET /api/workflows` - 获取所有工作流
- `POST /api/workflows` - 创建新工作流
- `GET /api/workflows/{id}` - 获取工作流详情
- `PUT /api/workflows/{id}` - 更新工作流
- `DELETE /api/workflows/{id}` - 删除工作流

### 工作流执行
- `POST /api/workflows/{id}/execute` - 执行工作流
- `GET /api/workflows/executions/{id}` - 获取执行状态

---

🎉 **恭喜！** 你的可视化工作流编排系统已经成功运行！