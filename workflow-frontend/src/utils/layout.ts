// src/utils/layout.ts
import dagre from 'dagre'
import type { Edge, Node } from '@vue-flow/core'

type Direction = 'TB' | 'BT' | 'LR' | 'RL'

interface LayoutOpts {
  rankdir?: Direction
  nodeSep?: number
  rankSep?: number
}

const NODE_WIDTH = 280      // 320→280 更紧凑
const NODE_HEIGHT = 56      // 64→56 视觉更轻

export function applyDagreLayout(nodes: Node[], edges: Edge[], opts?: LayoutOpts) {
  const g = new dagre.graphlib.Graph()
  // 默认上下布局更像流程图
  g.setGraph({
    rankdir: opts?.rankdir ?? 'TB',
    nodesep: opts?.nodeSep ?? 40,
    ranksep: opts?.rankSep ?? 80,
    marginx: 40,
    marginy: 40
  })
  g.setDefaultEdgeLabel(() => ({}))

  // 添加节点到图
  nodes.forEach(n => {
    g.setNode(n.id, { width: NODE_WIDTH, height: NODE_HEIGHT })
  })
  
  // 添加边到图
  edges.forEach(e => {
    g.setEdge(e.source, e.target)
  })

  // 执行布局算法
  dagre.layout(g)

  // 应用布局结果到节点
  const laidNodes: Node[] = nodes.map(n => {
    const nodeWithPosition = g.node(n.id)
    return {
      ...n,
      // dagre 给的是中心点，vue-flow 用左上角坐标
      position: { 
        x: nodeWithPosition.x - NODE_WIDTH / 2, 
        y: nodeWithPosition.y - NODE_HEIGHT / 2 
      }
    }
  })

  // 返回布局后的节点和边
  return { nodes: laidNodes, edges }
}

// 简单的手动对齐功能
export function alignNodes(nodes: Node[], direction: 'horizontal' | 'vertical') {
  if (nodes.length === 0) return nodes
  
  if (direction === 'horizontal') {
    // 水平对齐 - 使用第一个节点的y坐标
    const y = nodes[0].position.y
    return nodes.map(node => ({
      ...node,
      position: { ...node.position, y }
    }))
  } else {
    // 垂直对齐 - 使用第一个节点的x坐标
    const x = nodes[0].position.x
    return nodes.map(node => ({
      ...node,
      position: { ...node.position, x }
    }))
  }
}

// 分布节点均匀间距
export function distributeNodes(nodes: Node[], direction: 'horizontal' | 'vertical', spacing = 150) {
  if (nodes.length === 0) return nodes
  
  // 按位置排序
  const sortedNodes = [...nodes].sort((a, b) => {
    return direction === 'horizontal' 
      ? a.position.x - b.position.x
      : a.position.y - b.position.y
  })
  
  // 从第一个节点开始，按间距分布
  const start = direction === 'horizontal' 
    ? sortedNodes[0].position.x 
    : sortedNodes[0].position.y
  
  return sortedNodes.map((node, index) => ({
    ...node,
    position: direction === 'horizontal'
      ? { ...node.position, x: start + index * spacing }
      : { ...node.position, y: start + index * spacing }
  }))
}