<template>
  <section>
    <div class="page-title">
      <div>
        <h2>Workspace</h2>
        <p>Operate the full MVP loop: knowledge, review, drafting, and archive.</p>
      </div>
      <span class="tag success">MVP</span>
    </div>

    <div class="grid-3">
      <div class="panel kpi">
        <strong>{{ stats.projects }}</strong>
        <span>Bid archives</span>
      </div>
      <div class="panel kpi">
        <strong>{{ stats.knowledge }}</strong>
        <span>Knowledge items</span>
      </div>
      <div class="panel kpi">
        <strong>{{ health }}</strong>
        <span>Backend status</span>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <h3>Recommended Flow</h3>
        <ol class="flow-list">
          <li><strong>Build knowledge base</strong><span>Add qualifications, staff, references, and proposal materials.</span></li>
          <li><strong>Review bid document</strong><span>Find compliance gaps, fatal risks, negative deviations, and missing scoring points.</span></li>
          <li><strong>Generate proposal draft</strong><span>Create editable sections from tender requirements and enterprise context.</span></li>
          <li><strong>Archive result</strong><span>Record bid result, review reports, drafts, and lessons learned.</span></li>
        </ol>
      </div>
      <div class="panel">
        <h3>Quick Actions</h3>
        <div class="button-row">
          <button class="primary" @click="$emit('navigate', 'review')">Start Review</button>
          <button @click="$emit('navigate', 'draft')">Generate Draft</button>
          <button @click="$emit('navigate', 'knowledge')">Knowledge Base</button>
          <button @click="$emit('navigate', 'archive')">Archive</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api } from '../api/client';

defineEmits<{ navigate: [value: string] }>();

const health = ref('Checking');
const stats = reactive({ projects: 0, knowledge: 0 });

onMounted(async () => {
  try {
    await api.health();
    health.value = 'OK';
    stats.projects = (await api.listProjects()).length;
    stats.knowledge = (await api.listKnowledge()).length;
  } catch {
    health.value = 'Error';
  }
});
</script>
