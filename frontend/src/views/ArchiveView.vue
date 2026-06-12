<template>
  <section>
    <div class="page-title">
      <div>
        <h2>Bid Archive</h2>
        <p>Record tender projects, bid files, review reports, results, scores, and lessons learned.</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <h3>Add Project</h3>
        <label>Project Name<input v-model="form.projectName" class="control" /></label>
        <label>Tender No.<input v-model="form.tenderNo" class="control" /></label>
        <label>Tenderer<input v-model="form.tenderer" class="control" /></label>
        <div class="inline-fields">
          <label>Industry<input v-model="form.industry" class="control" /></label>
          <label>Region<input v-model="form.region" class="control" /></label>
        </div>
        <div class="inline-fields">
          <label>Budget<input v-model.number="form.budgetAmount" class="control" type="number" min="0" /></label>
          <label>Bid Amount<input v-model.number="form.bidAmount" class="control" type="number" min="0" /></label>
        </div>
        <div class="inline-fields">
          <label>Status
            <select v-model="form.status" class="control">
              <option>To Evaluate</option>
              <option>Drafting</option>
              <option>Reviewing</option>
              <option>Submitted</option>
              <option>Archived</option>
            </select>
          </label>
          <label>Result
            <select v-model="form.result" class="control">
              <option>Pending</option>
              <option>Won</option>
              <option>Lost</option>
              <option>Rejected</option>
            </select>
          </label>
        </div>
        <label>Notes<textarea v-model="form.notes" class="control" rows="4" /></label>
        <button class="primary" :disabled="saving" @click="save">{{ saving ? 'Saving...' : 'Save Archive' }}</button>
      </div>

      <div class="panel">
        <h3>Projects</h3>
        <table class="data-table clickable">
          <thead><tr><th>Project</th><th>Status</th><th>Result</th><th>Bid Amount</th></tr></thead>
          <tbody>
            <tr v-for="project in projects" :key="project.id" @click="select(project)">
              <td>{{ project.projectName }}</td>
              <td>{{ project.status }}</td>
              <td>{{ project.result || '-' }}</td>
              <td>{{ project.bidAmount || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="archive" class="panel">
      <h3>Archive Detail: {{ archive.project.projectName }}</h3>
      <div class="detail-grid">
        <span>Tender No.</span><strong>{{ archive.project.tenderNo || '-' }}</strong>
        <span>Tenderer</span><strong>{{ archive.project.tenderer || '-' }}</strong>
        <span>Status</span><strong>{{ archive.project.status || '-' }}</strong>
        <span>Review Reports</span><strong>{{ archive.reviews.length }}</strong>
        <span>Drafts</span><strong>{{ archive.drafts.length }}</strong>
        <span>Result</span><strong>{{ archive.project.result || '-' }}</strong>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api, type BidProject } from '../api/client';

const saving = ref(false);
const projects = ref<BidProject[]>([]);
const archive = ref<{ project: BidProject; reviews: unknown[]; drafts: unknown[] } | null>(null);
const form = reactive<BidProject>({ projectName: '', status: 'To Evaluate', result: 'Pending' });

async function load() {
  projects.value = await api.listProjects();
}

async function save() {
  if (!form.projectName) {
    alert('Please fill project name.');
    return;
  }
  saving.value = true;
  try {
    await api.createProject({ ...form });
    Object.assign(form, { projectName: '', tenderNo: '', tenderer: '', industry: '', region: '', budgetAmount: undefined, bidAmount: undefined, status: 'To Evaluate', result: 'Pending', notes: '' });
    await load();
    alert('Archive saved.');
  } finally {
    saving.value = false;
  }
}

async function select(row: BidProject) {
  if (row.id) {
    archive.value = await api.archive(row.id);
  }
}

onMounted(load);
</script>
