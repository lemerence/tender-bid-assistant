<template>
  <section>
    <div class="page-title">
      <div>
        <h2>AI Proposal Drafting</h2>
        <p>Generate editable proposal sections from tender requirements and enterprise materials.</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <label>Document Title<input v-model="form.title" class="control" /></label>
        <label>Section
          <select v-model="form.section" class="control">
            <option>Business Response</option>
            <option>Technical Plan</option>
            <option>Implementation Plan</option>
            <option>After-sales Service</option>
            <option>Deviation Statement</option>
          </select>
        </label>
        <label>Project
          <select v-model="form.projectId" class="control">
            <option :value="undefined">None</option>
            <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.projectName }}</option>
          </select>
        </label>
        <label>Tender Requirements<textarea v-model="form.tenderText" class="control" rows="7" /></label>
        <label>Knowledge Context<textarea v-model="form.knowledgeContext" class="control" rows="5" /></label>
        <label>Extra Requirements<textarea v-model="form.userRequirement" class="control" rows="3" /></label>
        <button class="primary" :disabled="loading" @click="submit">{{ loading ? 'Generating...' : 'Generate Draft' }}</button>
      </div>
      <div class="panel">
        <h3>Draft Result</h3>
        <p v-if="!result" class="empty">No draft yet.</p>
        <div v-else class="result-box">{{ result.content }}</div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api, type BidProject, type DraftResponse } from '../api/client';

const loading = ref(false);
const result = ref<DraftResponse | null>(null);
const projects = ref<BidProject[]>([]);
const form = reactive({
  projectId: undefined as number | undefined,
  title: 'Proposal Draft',
  section: 'Technical Plan',
  tenderText: '',
  knowledgeContext: '',
  userRequirement: '',
});

async function submit() {
  if (!form.title || !form.tenderText) {
    alert('Please fill title and tender requirements.');
    return;
  }
  loading.value = true;
  try {
    result.value = await api.draft({ ...form });
    alert('Draft generated and archived.');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  projects.value = await api.listProjects();
});
</script>
