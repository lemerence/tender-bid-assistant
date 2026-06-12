<template>
  <section>
    <div class="page-title">
      <div>
        <h2>AI Bid Review</h2>
        <p>Compare tender requirements with a prepared bid document and generate risk findings.</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <label>Review Title<input v-model="form.title" class="control" /></label>
        <label>Project
          <select v-model="form.projectId" class="control">
            <option :value="undefined">None</option>
            <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.projectName }}</option>
          </select>
        </label>
        <label>Tender Text<textarea v-model="form.tenderText" class="control" rows="9" /></label>
        <label>Bid Text<textarea v-model="form.bidText" class="control" rows="9" /></label>
        <button class="primary" :disabled="loading" @click="submit">{{ loading ? 'Reviewing...' : 'Start Review' }}</button>
      </div>

      <div class="panel">
        <h3>Review Report</h3>
        <p v-if="!result" class="empty">No result yet.</p>
        <template v-else>
          <div class="alert" :class="alertType">{{ result.summary }}</div>
          <h4>Issues</h4>
          <table class="data-table">
            <thead><tr><th>Type</th><th>Severity</th><th>Finding</th><th>Suggestion</th></tr></thead>
            <tbody>
              <tr v-for="issue in result.issues" :key="issue.category + issue.finding">
                <td>{{ issue.category }}</td>
                <td><span :class="issue.severity === 'High' || issue.severity === 'Fatal' || issue.severity === '高' || issue.severity === '致命' ? 'issue-high' : 'issue-medium'">{{ issue.severity }}</span></td>
                <td>{{ issue.finding }}</td>
                <td>{{ issue.suggestion }}</td>
              </tr>
            </tbody>
          </table>
          <h4>Checklist</h4>
          <label v-for="item in result.checklist" :key="item" class="check-line"><input type="checkbox" /> {{ item }}</label>
        </template>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { api, type BidProject, type ReviewResponse } from '../api/client';

const loading = ref(false);
const result = ref<ReviewResponse | null>(null);
const projects = ref<BidProject[]>([]);
const form = reactive({ projectId: undefined as number | undefined, title: 'Bid Document Review', tenderText: '', bidText: '' });

const alertType = computed(() => {
  if (!result.value) return 'info';
  return result.value.riskLevel === '高' || result.value.riskLevel === '致命' ? 'error' : result.value.riskLevel === '中' ? 'warning' : 'success';
});

async function submit() {
  if (!form.tenderText || !form.bidText) {
    alert('Please fill tender text and bid text.');
    return;
  }
  loading.value = true;
  try {
    result.value = await api.review({ ...form });
    alert('Review completed and archived.');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  projects.value = await api.listProjects();
});
</script>
