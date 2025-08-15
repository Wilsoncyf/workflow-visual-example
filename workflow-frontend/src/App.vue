<template>
  <div id="app">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <div class="logo">
          <el-icon size="24" color="#1890ff"><Connection /></el-icon>
          <span>FlowDesigner</span>
        </div>
        <div class="workflow-info">
          <el-select 
            v-model="selectedWorkflowId" 
            placeholder="选择工作流" 
            class="workflow-select"
            @change="onWorkflowSelect"
          >
            <el-option
              v-for="workflow in workflows"
              :key="workflow.id"
              :label="workflow.name"
              :value="workflow.id"
            />
          </el-select>
          <span v-if="currentWorkflow.name" class="current-workflow">{{ currentWorkflow.name }}</span>
        </div>
      </div>
      
      <div class="toolbar-center">
        <el-button-group>
          <el-button @click="newWorkflow" :icon="DocumentAdd" type="primary" plain>新建</el-button>
          <el-button @click="loadWorkflow" :icon="FolderOpened" :disabled="!selectedWorkflowId" plain>打开</el-button>
          <el-button @click="saveWorkflow" :icon="DocumentChecked" type="success">保存</el-button>
        </el-button-group>
      </div>
      
      <div class="toolbar-right">
        <el-button @click="executeWorkflow" :icon="VideoPlay" type="warning" :disabled="!currentWorkflow.id">
          执行工作流
        </el-button>
        <el-button @click="deleteWorkflow" :icon="Delete" type="danger" text :disabled="!selectedWorkflowId">
          删除
        </el-button>
      </div>
    </div>
    
    <!-- 主要内容区域 -->
    <div class="main-layout">
      <!-- 左侧边栏 - 节点库 -->
      <div class="left-sidebar">
        <div class="sidebar-section">
          <div class="section-header">
            <el-icon><Cpu /></el-icon>
            <span>节点组件</span>
          </div>
          <div class="node-palette">
            <div
              v-for="node in nodePalette"
              :key="node.type"
              :class="['node-palette-item', `node-${node.type}`]"
              draggable="true"
              @dragstart="onDragStart($event, node)"
            >
              <div class="node-icon">
                <el-icon><component :is="node.icon" /></el-icon>
              </div>
              <div class="node-info">
                <div class="node-name">{{ node.label }}</div>
                <div class="node-desc">{{ node.description }}</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 执行状态 -->
        <div class="sidebar-section">
          <div class="section-header">
            <el-icon><DataAnalysis /></el-icon>
            <span>执行状态</span>
          </div>
          <div class="execution-status">
            <div v-if="lastExecution" class="status-card">
              <div class="status-badge" :class="lastExecution.status">
                {{ getStatusText(lastExecution.status) }}
              </div>
              <div class="status-details">
                <div class="detail-item">
                  <span>开始时间</span>
                  <span>{{ formatTime(lastExecution.startedAt) }}</span>
                </div>
                <div v-if="lastExecution.duration" class="detail-item">
                  <span>执行时长</span>
                  <span>{{ lastExecution.duration }}ms</span>
                </div>
              </div>
            </div>
            <div v-else class="no-execution">
              <el-icon><Clock /></el-icon>
              <span>暂无执行记录</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中央画布区域 -->
      <div class="canvas-area">
        <div class="canvas-header">
          <div class="canvas-title">
            <el-icon><EditPen /></el-icon>
            <span>工作流设计器</span>
          </div>
          <div class="canvas-tools">
            <el-button-group size="small">
              <el-button @click="autoLayout" title="自动布局">
                <el-icon><Grid /></el-icon>
              </el-button>
              <el-button @click="fitView" :icon="FullScreen" circle title="适应视图" />
              <el-button @click="zoomIn" :icon="ZoomIn" circle title="放大" />
              <el-button @click="zoomOut" :icon="ZoomOut" circle title="缩小" />
            </el-button-group>
          </div>
        </div>
        
        <div class="flow-shell">
          <div class="flow-container">
            <VueFlow
              v-model:nodes="nodes"
              v-model:edges="edges"
              :node-types="nodeTypes"
            @connect="onConnect"
            @drop="onDrop"
            @dragover.prevent
            @node-click="onNodeClick"
            @edge-click="onEdgeClick"
            :connection-line-options="{ type: 'smoothstep' as any, style: { strokeWidth: 2, stroke: '#10b981' } }"
            :default-edge-options="defaultEdgeOptions"
            :fit-view-on-init="true"
            :nodes-draggable="true"
            :nodes-connectable="true"
            :elements-selectable="true"
            :connect-on-click="false"
            :snap-to-grid="true"
            :snap-grid="[10, 10]"
            :connection-mode="ConnectionMode.Strict"
            :elevate-edges-on-select="true"
            :is-valid-connection="isValidConnection"
            class="vue-flow-container"
          >
            <Background 
              pattern-color="#e1e5e9" 
              :gap="20 as any" 
              variant="dots" 
            />
            <Controls 
              show-zoom 
              show-fit-view 
              show-interactive 
              position="bottom-left"
            />
            <MiniMap 
              node-color="#4f46e5" 
              :node-border-radius="8" 
              position="bottom-right"
            />
          </VueFlow>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 - 属性面板 -->
      <div class="right-sidebar" v-show="selectedNode">
        <div class="sidebar-section">
          <div class="section-header">
            <el-icon><Setting /></el-icon>
            <span>节点配置</span>
          </div>
          
          <div class="property-form" v-if="selectedNode">
            <el-form label-position="top" size="small">
              <el-form-item label="节点ID">
                <el-input v-model="selectedNode.id" disabled />
              </el-form-item>
              
              <el-form-item label="显示名称">
                <el-input 
                  v-model="selectedNode.data.label" 
                  @change="updateNode"
                  placeholder="输入节点名称"
                />
              </el-form-item>
              
              <el-form-item label="节点类型">
                <el-tag :type="getNodeTypeColor(selectedNode.data.nodeType)">
                  {{ getNodeTypeName(selectedNode.data.nodeType) }}
                </el-tag>
              </el-form-item>
              
              <el-form-item label="位置信息">
                <div class="position-info">
                  <el-input-number 
                    v-model="selectedNode.position.x" 
                    size="small" 
                    :precision="0"
                    @change="updateNodePosition"
                  />
                  <span class="position-separator">×</span>
                  <el-input-number 
                    v-model="selectedNode.position.y" 
                    size="small" 
                    :precision="0"
                    @change="updateNodePosition"
                  />
                </div>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, markRaw, nextTick } from 'vue'
import { VueFlow, useVueFlow, ConnectionMode, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import { MiniMap } from '@vue-flow/minimap'
import { ElMessage, ElMessageBox } from 'element-plus'
import CustomNode from './components/CustomNode.vue'
import { applyDagreLayout } from './utils/layout'
import { 
  DocumentAdd, 
  FolderOpened, 
  DocumentChecked, 
  VideoPlay, 
  Delete,
  Cpu,
  Connection,
  DataAnalysis,
  Finished,
  Setting,
  EditPen,
  FullScreen,
  ZoomIn,
  ZoomOut,
  Clock,
  Grid
} from '@element-plus/icons-vue'
import axios from 'axios'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

const API_BASE_URL = 'http://localhost:8080/api'

const nodes = ref<any[]>([])
const edges = ref<any[]>([])
const selectedWorkflowId = ref<string>('')
const workflows = ref<any[]>([])
const currentWorkflow = ref<any>({ id: '', name: '', description: '' })
const selectedNode = ref<any>(null)
const lastExecution = ref<any>(null)

const nodePalette = [
  { 
    type: 'input', 
    label: '输入节点', 
    description: '工作流的起始点',
    icon: Connection 
  },
  { 
    type: 'process', 
    label: '处理节点', 
    description: '执行数据处理逻辑',
    icon: Cpu 
  },
  { 
    type: 'delay', 
    label: '延迟节点', 
    description: '模拟耗时操作',
    icon: DataAnalysis 
  },
  { 
    type: 'output', 
    label: '输出节点', 
    description: '工作流的结束点',
    icon: Finished 
  }
]

const { addNodes, addEdges, project, fitView, zoomIn, zoomOut, getNodes } = useVueFlow()

// 注册自定义节点类型
const nodeTypes = {
  custom: markRaw(CustomNode as any)
}

// 默认边样式配置
const defaultEdgeOptions = {
  type: 'smoothstep',
  animated: false,
  markerEnd: {
    type: MarkerType.ArrowClosed,
    width: 18,
    height: 18,
    color: '#4f46e5'
  },
  style: {
    strokeWidth: 2.5,
    stroke: '#4f46e5'   // 和句柄 hover 颜色一致
  }
}

const isValidConnection = (c:any) => {
  // 句柄必须对、不能同向
  if (c.sourceHandle !== 'out' || c.targetHandle !== 'in') return false

  // 不允许自己连自己
  if (c.source === c.target) return false

  // 可选：禁止产生环（保持 DAG）
  const createsCycle = edges.value.some(e =>
    (e.source === c.target && e.target === c.source)
  )
  if (createsCycle) return false

  // 可选：根据节点类型再限制
  const src = nodes.value.find(n => n.id === c.source)
  const dst = nodes.value.find(n => n.id === c.target)
  if (!src || !dst) return false
  if (src.data?.nodeType === 'output') return false
  if (dst.data?.nodeType === 'input')  return false

  return true
}

onMounted(async () => {
  loadWorkflows()
  await initDefaultFlow()
  await autoLayout()
})

const initDefaultFlow = async () => {
  const initialNodes = [
    {
      id: '1',
      type: 'custom',
      position: { x: 0, y: 0 }, // 位置将由自动布局确定
      data: { 
        label: '开始',
        nodeType: 'input'
      }
    },
    {
      id: '2',
      type: 'custom',
      position: { x: 0, y: 0 },
      data: { 
        label: '处理',
        nodeType: 'process'
      }
    },
    {
      id: '3',
      type: 'custom',
      position: { x: 0, y: 0 },
      data: { 
        label: '结束',
        nodeType: 'output'
      }
    }
  ]
  
  const initialEdges = [
    {
      id: 'e-1-2',
      source: '1',
      sourceHandle: 'out',
      target: '2',
      targetHandle: 'in',
      type: 'smoothstep'
    },
    {
      id: 'e-2-3',
      source: '2',
      sourceHandle: 'out',
      target: '3',
      targetHandle: 'in',
      type: 'smoothstep'
    }
  ]
  
  nodes.value = initialNodes
  edges.value = initialEdges
}

const loadWorkflows = async () => {
  try {
    console.log('Loading workflows from:', `${API_BASE_URL}/workflows`)
    const response = await axios.get(`${API_BASE_URL}/workflows`)
    console.log('Workflows loaded:', response.data)
    workflows.value = response.data
  } catch (error) {
    console.error('Failed to load workflows:', error)
    ElMessage.error('无法连接到后端服务，请确保后端服务已启动')
  }
}

const newWorkflow = () => {
  ElMessageBox.prompt('请输入工作流名称', '新建工作流', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
  }).then(({ value }) => {
    currentWorkflow.value = { id: '', name: value, description: '' }
    initDefaultFlow()
    ElMessage.success('已创建新工作流')
  }).catch(() => {})
}

const loadWorkflow = async () => {
  if (!selectedWorkflowId.value) return
  
  try {
    const response = await axios.get(`${API_BASE_URL}/workflows/${selectedWorkflowId.value}`)
    const workflow = response.data
    currentWorkflow.value = {
      id: workflow.id,
      name: workflow.name,
      description: workflow.description
    }
    nodes.value = workflow.nodes || []
    edges.value = workflow.edges || []
    await nextTick()
    fitView({ padding: 0.2 })
    ElMessage.success('工作流加载成功')
  } catch (error) {
    ElMessage.error('加载工作流失败')
    console.error(error)
  }
}

const saveWorkflow = async () => {
  try {
    let name = currentWorkflow.value.name
    
    if (!name) {
      const result = await ElMessageBox.prompt('请输入工作流名称', '保存工作流', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
      })
      name = result.value
    }
    
    if (!name) return
    
    const workflowData = {
      name,
      description: currentWorkflow.value.description || '',
      nodes: nodes.value,
      edges: edges.value,
      metadata: {}
    }
    
    console.log('Saving workflow:', workflowData)
    
    let response
    if (currentWorkflow.value.id) {
      console.log('Updating workflow:', currentWorkflow.value.id)
      response = await axios.put(`${API_BASE_URL}/workflows/${currentWorkflow.value.id}`, workflowData)
    } else {
      console.log('Creating new workflow')
      response = await axios.post(`${API_BASE_URL}/workflows`, workflowData)
    }
    
    console.log('Save response:', response.data)
    
    currentWorkflow.value = {
      id: response.data.id,
      name: response.data.name,
      description: response.data.description || ''
    }
    
    await loadWorkflows()
    selectedWorkflowId.value = response.data.id
    ElMessage.success('工作流保存成功')
  } catch (error: any) {
    console.error('Save workflow error:', error)
    ElMessage.error('保存工作流失败: ' + (error.response?.data?.message || error.message))
  }
}

const executeWorkflow = async () => {
  if (!currentWorkflow.value.id) {
    ElMessage.warning('请先保存工作流')
    return
  }
  
  try {
    const inputs = { input: 'test data', timestamp: Date.now() }
    const response = await axios.post(
      `${API_BASE_URL}/workflows/${currentWorkflow.value.id}/execute`,
      inputs
    )
    
    lastExecution.value = response.data
    ElMessage.success('工作流执行已启动')
    
    // 轮询检查执行状态
    const checkStatus = setInterval(async () => {
      try {
        const statusResponse = await axios.get(
          `${API_BASE_URL}/workflows/executions/${response.data.id}`
        )
        lastExecution.value = statusResponse.data
        
        if (statusResponse.data.status === 'completed') {
          ElMessage.success('工作流执行完成')
          clearInterval(checkStatus)
        } else if (statusResponse.data.status === 'failed') {
          ElMessage.error('工作流执行失败: ' + statusResponse.data.errorMessage)
          clearInterval(checkStatus)
        }
      } catch (error) {
        console.error('Failed to check status:', error)
        clearInterval(checkStatus)
      }
    }, 2000)
    
  } catch (error) {
    ElMessage.error('执行工作流失败')
    console.error(error)
  }
}

const deleteWorkflow = async () => {
  if (!selectedWorkflowId.value) return
  
  try {
    await ElMessageBox.confirm('确定要删除这个工作流吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    await axios.delete(`${API_BASE_URL}/workflows/${selectedWorkflowId.value}`)
    ElMessage.success('工作流已删除')
    selectedWorkflowId.value = ''
    currentWorkflow.value = { id: '', name: '', description: '' }
    initDefaultFlow()
    await loadWorkflows()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除工作流失败')
      console.error(error)
    }
  }
}

const onDragStart = (event: DragEvent, node: any) => {
  event.dataTransfer!.effectAllowed = 'move'
  event.dataTransfer!.setData('nodeType', node.type)
  event.dataTransfer!.setData('nodeLabel', node.label)
}

const onDrop = (event: DragEvent) => {
  const type = event.dataTransfer!.getData('nodeType')
  const label = event.dataTransfer!.getData('nodeLabel')
  
  const position = project({
    x: event.clientX - 250,
    y: event.clientY - 100
  })
  
  const newNode = {
    id: `node-${Date.now()}`,
    type: 'custom',  // 使用自定义节点类型
    position,
    data: { 
      label,
      nodeType: type  // 保存原始节点类型用于样式
    }
  }
  
  console.log('Adding new node:', newNode)
  addNodes([newNode])
  ElMessage.success(`已添加${label}`)
}

const onConnect = (params: any) => {
  console.log('Connecting nodes:', params)
  const newEdge = {
    ...params,
    id: `e-${params.source}-${params.target}-${Date.now()}`,
    sourceHandle: 'out',
    targetHandle: 'in',
    type: 'smoothstep'
  }
  console.log('Adding edge:', newEdge)
  addEdges([newEdge])
  ElMessage.success(`已连接: ${params.source} → ${params.target}`)
}

const onNodeClick = (nodeMouseEvent: any) => {
  selectedNode.value = nodeMouseEvent.node
}

const onEdgeClick = () => {
  selectedNode.value = null
}

const updateNode = () => {
  // Node is automatically updated through v-model
}

// 新增的辅助方法
const onWorkflowSelect = () => {
  // 工作流选择事件处理
}

const getStatusText = (status: string) => {
  const statusMap: Record<string, string> = {
    'running': '执行中',
    'completed': '已完成',
    'failed': '执行失败',
    'pending': '等待中'
  }
  return statusMap[status] || status
}

const formatTime = (time: string) => {
  return new Date(time).toLocaleString()
}

const getNodeTypeColor = (nodeType: string) => {
  const colorMap: Record<string, string> = {
    'input': 'success',
    'process': 'primary',
    'delay': 'warning',
    'output': 'danger'
  }
  return colorMap[nodeType] || 'info'
}

const getNodeTypeName = (nodeType: string) => {
  const nameMap: Record<string, string> = {
    'input': '输入节点',
    'process': '处理节点',
    'delay': '延迟节点',
    'output': '输出节点'
  }
  return nameMap[nodeType] || nodeType
}

const updateNodePosition = () => {
  // Position is automatically updated through v-model
}

// 自动布局当前画布上的节点
const autoLayout = async () => {
  if (nodes.value.length === 0) {
    ElMessage.warning('画布上没有节点')
    return
  }
  
  await nextTick()                    // 等渲染
  const sizeMap = Object.fromEntries(
    getNodes.value.map(n => [n.id, {
      width:  n.dimensions?.width  ?? 220,
      height: n.dimensions?.height ?? 56,
    }])
  )
  
  const { nodes: layoutedNodes, edges: layoutedEdges } = applyDagreLayout(
    nodes.value, 
    edges.value,
    { rankdir: 'TB', nodeSep: 40, rankSep: 80 },
    sizeMap
  )
  
  nodes.value = layoutedNodes
  edges.value = layoutedEdges
  
  // 使用nextTick等待DOM更新后再适应视图
  await nextTick()
  fitView({ padding: 0.24 })
  
  ElMessage.success('已自动布局')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 
               'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  background: #f8fafc;
}

/* 顶部工具栏样式 */
.toolbar {
  height: 64px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
}

.workflow-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.workflow-select {
  min-width: 200px;
}

.current-workflow {
  font-size: 14px;
  opacity: 0.9;
}

.toolbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 主布局样式 */
.main-layout {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 左侧边栏样式 */
.left-sidebar {
  width: 280px;
  background: white;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-section {
  padding: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}

.node-palette {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.node-palette-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  cursor: move;
  transition: all 0.2s ease;
}

.node-palette-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.node-input {
  border-left: 4px solid #10b981;
}

.node-process {
  border-left: 4px solid #3b82f6;
}

.node-delay {
  border-left: 4px solid #f59e0b;
}

.node-output {
  border-left: 4px solid #ef4444;
}

.node-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: white;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.node-info {
  flex: 1;
}

.node-name {
  font-weight: 500;
  font-size: 14px;
  color: #111827;
  margin-bottom: 2px;
}

.node-desc {
  font-size: 12px;
  color: #6b7280;
}

/* 执行状态样式 */
.execution-status {
  min-height: 100px;
}

.status-card {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e5e7eb;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 8px;
}

.status-badge.running {
  background: #dbeafe;
  color: #1d4ed8;
}

.status-badge.completed {
  background: #dcfce7;
  color: #166534;
}

.status-badge.failed {
  background: #fecaca;
  color: #dc2626;
}

.status-details {
  space-y: 4px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  margin-bottom: 4px;
}

.detail-item span:first-child {
  color: #6b7280;
}

.detail-item span:last-child {
  color: #111827;
  font-weight: 500;
}

.no-execution {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px;
  color: #9ca3af;
  font-size: 14px;
}

/* 中央画布区域样式 */
.canvas-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  overflow: hidden;
}

.canvas-header {
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
}

.canvas-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
}

.canvas-tools {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 关键：只约束外层容器本身，不影响Vue Flow内部元素 */
.flow-shell {
  flex: 1;
  position: relative;
  overflow: hidden;
  transform: none !important;
  zoom: 1 !important;
}

.flow-container {
  position: relative;
  width: 100%;
  height: calc(100vh - 64px - 48px); /* 减去toolbar和canvas-header的高度 */
  min-height: 400px;
  background: #fafbfc;
  overflow: hidden;
}

.vue-flow-container {
  width: 100%;
  height: 100%;
}

/* 右侧边栏样式 */
.right-sidebar {
  width: 320px;
  background: white;
  border-left: 1px solid #e5e7eb;
  overflow-y: auto;
}

.property-form {
  padding: 16px;
}

.position-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.position-separator {
  font-size: 12px;
  color: #9ca3af;
}


/* 连接手柄样式 */





@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: .5;
  }
}

/* 边线样式 */
.vue-flow__edge-path {
  stroke: #6366f1 !important;
  stroke-width: 2 !important;
  fill: none !important;
  filter: drop-shadow(0 1px 3px rgba(0, 0, 0, 0.1));
  transition: all 0.2s ease !important;
}

.vue-flow__edge:hover .vue-flow__edge-path {
  stroke: #4f46e5 !important;
  stroke-width: 3 !important;
  filter: drop-shadow(0 2px 6px rgba(99, 102, 241, 0.3)) !important;
}

.vue-flow__edge.selected .vue-flow__edge-path {
  stroke: #10b981 !important;
  stroke-width: 3 !important;
  filter: drop-shadow(0 2px 6px rgba(16, 185, 129, 0.3)) !important;
}

/* 动画效果 */
.vue-flow__edge.animated .vue-flow__edge-path {
  stroke-dasharray: 5 !important;
  animation: dash-flow 1s linear infinite !important;
}

@keyframes dash-flow {
  to {
    stroke-dashoffset: -10;
  }
}

/* 箭头样式 */
.vue-flow__arrowhead {
  fill: #6366f1 !important;
  stroke: #6366f1 !important;
}

.vue-flow__edge:hover .vue-flow__arrowhead {
  fill: #4f46e5 !important;
  stroke: #4f46e5 !important;
}

.vue-flow__edge.selected .vue-flow__arrowhead {
  fill: #10b981 !important;
  stroke: #10b981 !important;
}

/* 连接线样式 */
.vue-flow__connection-line {
  stroke: #10b981;
  stroke-width: 3;
  stroke-dasharray: 8,4;
  animation: dash 1s linear infinite;
}

@keyframes dash {
  to {
    stroke-dashoffset: -12;
  }
}

/* 背景网格样式 */
.vue-flow__background {
  background-color: #f8fafc;
}

/* 控制面板样式 */
.vue-flow__controls {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
}

.vue-flow__controls-button {
  background: white;
  border: none;
  border-bottom: 1px solid #e2e8f0;
  color: #64748b;
  transition: all 0.2s ease;
}

.vue-flow__controls-button:hover {
  background: #f8fafc;
  color: #3b82f6;
}

.vue-flow__controls-button:last-child {
  border-bottom: none;
}

/* 小地图样式 */
.vue-flow__minimap {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

/* 选择框样式 */
.vue-flow__selection {
  background: rgba(59, 130, 246, 0.1);
  border: 2px solid #3b82f6;
  border-radius: 4px;
}

/* 节点标签样式 */
.vue-flow__node-default .vue-flow__node-content,
.vue-flow__node-input .vue-flow__node-content,
.vue-flow__node-output .vue-flow__node-content {
  font-weight: 500;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .left-sidebar,
  .right-sidebar {
    width: 260px;
  }
  
  .toolbar {
    padding: 0 16px;
  }
  
  .toolbar-left,
  .toolbar-right {
    gap: 8px;
  }
}

@media (max-width: 768px) {
  .left-sidebar {
    width: 240px;
  }
  
  .right-sidebar {
    display: none;
  }
  
  .toolbar {
    height: 56px;
    flex-direction: column;
    gap: 8px;
    padding: 8px 12px;
  }
}

/* 只使用自定义节点视觉，取消默认节点容器的白底/边框/阴影/内边距 */
.vue-flow__node { 
  background: transparent; 
  border: none; 
  box-shadow: none; 
  padding: 0; 
}
</style>
