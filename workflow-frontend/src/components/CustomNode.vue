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
.custom-node{
  position: relative;
  display: inline-flex;
  width: fit-content;
  overflow: visible;
}

/* 卡片本体 */
.card{
  position: relative;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 10px 16px;
  box-shadow: 0 6px 18px rgba(2,6,23,.06);
}

/* 左侧/右侧装饰条（可选视觉）*/
.node-input  .card::before,
.node-process .card::before,
.node-delay  .card::before,
.node-output .card::before{
  content:"";
  position:absolute;
  left:-12px;                  /* 与卡片脱开一点 */
  top:12px; bottom:12px;
  width:4px;
  border-radius:4px;
  pointer-events:none;         /* 不抢事件 */
}
.node-input  .card::before{ background:#16a34a; }
.node-process .card::before{ background:#2563eb; }
.node-delay  .card::before{ background:#f59e0b; }
.node-output .card::before{ background:#dc2626; }

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

/* 句柄：基于 .card 定位，正好贴在白卡片的边缘 */
.handle{
  position:absolute;
  z-index:20;
  pointer-events:auto;
  width:12px; height:12px;
  border-radius:50%;
  background:#fff;
  border:2px solid #334155;
  box-shadow:0 1px 4px rgba(0,0,0,.15);
  transition: box-shadow .12s ease, transform .12s ease;
}

/* 命中区扩大，不改变视觉 */
.handle::after{
  content:"";
  position:absolute;
  left:50%; top:50%;
  width:28px; height:28px;
  transform:translate(-50%,-50%);
  border-radius:50%;
}

/* 关键：以 .card 的内外边界作为参照系来"贴边" */
.handle-in{
  left: calc(-6px);                      /* 小圆点一半在外，一半在里，视觉正贴边 */
  top: 50%;
  transform: translateY(-50%);
}
.handle-out{
  right: calc(-6px);
  top: 50%;
  transform: translateY(-50%);
}

.handle:hover{
  box-shadow: 0 0 10px rgba(99,102,241,.35);
  border-color:#6366f1;
}
</style>