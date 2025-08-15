// src/utils/layout.ts
import dagre from 'dagre'
import type { Edge, Node } from '@vue-flow/core'

type Direction = 'TB' | 'BT' | 'LR' | 'RL'
interface LayoutOpts {
  rankdir?: Direction
  nodeSep?: number
  rankSep?: number
}
type SizeMap = Record<string, { width:number; height:number }>

export function applyDagreLayout(
  nodes: Node[],
  edges: Edge[],
  opts?: LayoutOpts,
  sizes?: SizeMap,                     // 新增
){
  const g = new dagre.graphlib.Graph()
  g.setGraph({
    rankdir: opts?.rankdir ?? 'TB',
    nodesep: opts?.nodeSep ?? 40,
    ranksep: opts?.rankSep ?? 80,
    marginx: 40,
    marginy: 40
  })
  g.setDefaultEdgeLabel(() => ({}))

  nodes.forEach(n => {
    const w = sizes?.[n.id]?.width  ?? Math.max(160, (n as any).dimensions?.width  ?? 220)
    const h = sizes?.[n.id]?.height ?? Math.max(48,  (n as any).dimensions?.height ?? 56)
    g.setNode(n.id, { width: w, height: h })
  })
  edges.forEach(e => g.setEdge(e.source, e.target))

  dagre.layout(g)

  const laidNodes: Node[] = nodes.map(n => {
    const p = g.node(n.id)
    return { ...n, position: { x: p.x - (p.width/2), y: p.y - (p.height/2) } }
  })

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