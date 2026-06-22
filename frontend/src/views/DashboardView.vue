<template>
  <section>
    <div class="page-title">
      <div>
        <h2>工作台</h2>
        <p>围绕企业知识库、AI审标、AI编标和标书归档完成投标全流程。</p>
      </div>
      <span class="tag success">MVP版本</span>
    </div>

    <div class="grid-3">
      <div class="panel kpi">
        <strong>{{ stats.projects }}</strong>
        <span>归档项目</span>
      </div>
      <div class="panel kpi">
        <strong>{{ stats.knowledge }}</strong>
        <span>知识条目</span>
      </div>
      <div class="panel kpi">
        <strong>{{ health }}</strong>
        <span>后端状态</span>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <h3>推荐流程</h3>
        <ol class="flow-list">
          <li><strong>建设企业知识库</strong><span>录入企业资质、人员能力、项目业绩和可复用标书素材。</span></li>
          <li><strong>审查已有标书</strong><span>发现响应缺口、废标风险、负偏离和遗漏的评分点。</span></li>
          <li><strong>生成标书初稿</strong><span>根据招标要求和企业知识库生成可编辑章节。</span></li>
          <li><strong>归档投标结果</strong><span>记录投标结果、审标报告、编标内容和复盘经验。</span></li>
        </ol>
      </div>
      <div class="panel">
        <h3>快捷入口</h3>
        <div class="button-row">
          <button class="primary" @click="$emit('navigate', 'review')">开始审标</button>
          <button @click="$emit('navigate', 'draft')">生成标书</button>
          <button @click="$emit('navigate', 'knowledge')">企业知识库</button>
          <button @click="$emit('navigate', 'archive')">标书归档</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api } from '../api/client';

defineEmits<{ navigate: [value: string] }>();

const health = ref('检查中');
const stats = reactive({ projects: 0, knowledge: 0 });

onMounted(async () => {
  try {
    await api.health();
    health.value = '正常';
    stats.projects = (await api.listProjects()).length;
    stats.knowledge = (await api.listKnowledge()).length;
  } catch {
    health.value = '异常';
  }
});
</script>
