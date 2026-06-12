<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">AI</div>
        <div>
          <h1>招投标助手</h1>
          <p>审标 · 编标 · 归档</p>
        </div>
      </div>
      <nav>
        <button v-for="item in navItems" :key="item.key" :class="{ active: current === item.key }" @click="current = item.key">
          <component :is="item.icon" :size="18" />
          <span>{{ item.label }}</span>
        </button>
      </nav>
    </aside>

    <main>
      <DashboardView v-if="current === 'dashboard'" @navigate="current = $event" />
      <KnowledgeView v-else-if="current === 'knowledge'" />
      <ReviewView v-else-if="current === 'review'" />
      <DraftView v-else-if="current === 'draft'" />
      <ArchiveView v-else />
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { Archive, BookOpen, FileCheck2, LayoutDashboard, PenLine } from 'lucide-vue-next';
import DashboardView from './views/DashboardView.vue';
import KnowledgeView from './views/KnowledgeView.vue';
import ReviewView from './views/ReviewView.vue';
import DraftView from './views/DraftView.vue';
import ArchiveView from './views/ArchiveView.vue';

const current = ref('dashboard');
const navItems = [
  { key: 'dashboard', label: '工作台', icon: LayoutDashboard },
  { key: 'knowledge', label: '企业知识库', icon: BookOpen },
  { key: 'review', label: 'AI审标', icon: FileCheck2 },
  { key: 'draft', label: 'AI编标', icon: PenLine },
  { key: 'archive', label: '标书归档', icon: Archive },
];
</script>
