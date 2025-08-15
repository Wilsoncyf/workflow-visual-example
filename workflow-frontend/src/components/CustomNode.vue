<template>
  <div :class="['custom-node', `node-${data.nodeType}`]">
    <div class="card">
      <!-- 左侧圆点：输入（贴在白边上） -->
      <Handle
        v-if="data.nodeType !== 'input'"
        id="in"
        type="target"
        :position="Position.Left"
        class="handle handle-in"
      />

      <span class="pill" :class="`pill-${data.nodeType}`">{{ data.label }}</span>

      <!-- 右侧圆点：输出（贴在白边上） -->
      <Handle
        v-if="data.nodeType !== 'output'"
        id="out"
        type="source"
        :position="Position.Right"
        class="handle handle-out"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Handle, Position } from '@vue-flow/core'

defineProps<{
  data: {
    label: string
    nodeType: 'input' | 'process' | 'delay' | 'output' | string
  }
}>()
</script>

<style scoped>
.custom-node {
  position: relative;
  display: inline-flex;              /* 宽度跟随内容 */
  align-items: center;
  width: fit-content;
  min-width: 160px;
  max-width: 420px;                  /* 控制别太长 */
  overflow: visible;
  /* 句柄尺寸：可按需调整 */
  --h: 12px; /* handle size */
}

/* 内部卡片样式 */
.card{
  position: relative;       /* 关键：句柄以卡片为定位容器 */
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 10px 16px;
  box-shadow: 0 6px 18px rgba(2,6,23,.06);
}

/* 左侧类型色条 */
.custom-node::before{
  content: "";
  position: absolute;
  left: -2px; top: 10px; bottom: 10px;
  width: 4px; border-radius: 3px;
  background: #94a3b8; /* 默认占位色 */
}
.node-input::before   { background:#16a34a; }
.node-process::before { background:#2563eb; }
.node-delay::before   { background:#f59e0b; }
.node-output::before  { background:#dc2626; }

/* 彩色胶囊 */
.pill{
  display: inline-block;
  padding: 6px 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  letter-spacing: .3px;
  white-space: nowrap;
  line-height: 1;
}
.pill-input   { background: #16a34a; }
.pill-process { background: #2563eb; }
.pill-delay   { background: #f59e0b; }
.pill-output  { background: #dc2626; }

/* 句柄样式 */
.handle{
  position: absolute;
  z-index: 20;
  pointer-events: all;
  width: var(--h); height: var(--h);
  border-radius: 50%;
  background: #fff;
  border: 2px solid #334155;
  box-shadow: 0 1px 4px rgba(0,0,0,.15);
  top: 50%;
  transform: translateY(-50%);   /* 精准垂直居中 */
}

/* 命中区稍放大（不改变视觉） */
.handle::after{
  content: "";
  position: absolute;
  left: 50%; top: 50%;
  width: calc(var(--h) + 14px);
  height: calc(var(--h) + 14px);
  transform: translate(-50%, -50%);
  border-radius: 50%;
  pointer-events: auto;
}

/* 关键：把圆心压在"白边"上，但仍留在盒子内 1px，命中稳定 */
.handle-in  { left:  calc(var(--h)/-2 + 1px); }   /* +1px 确保几何不越界 */
.handle-out { right: calc(var(--h)/-2 + 1px); }  /* 圆心基本压在白边中线 */

.handle:hover{
  box-shadow: 0 0 10px rgba(99,102,241,.4);
  border-color: #6366f1;
}
</style>