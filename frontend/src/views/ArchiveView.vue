<template>
  <section>
    <div class="page-title">
      <div>
        <h2>标书归档</h2>
        <p>记录招标项目、投标文件、审标报告、中标结果、报价和复盘信息。</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <h3>新增项目</h3>
        <label>项目名称<input v-model="form.projectName" class="control" /></label>
        <label>招标编号<input v-model="form.tenderNo" class="control" /></label>
        <label>招标人<input v-model="form.tenderer" class="control" /></label>
        <div class="inline-fields">
          <label>行业<input v-model="form.industry" class="control" /></label>
          <label>地区<input v-model="form.region" class="control" /></label>
        </div>
        <div class="inline-fields">
          <label>预算金额<input v-model.number="form.budgetAmount" class="control" type="number" min="0" /></label>
          <label>投标金额<input v-model.number="form.bidAmount" class="control" type="number" min="0" /></label>
        </div>
        <div class="inline-fields">
          <label>项目状态
            <select v-model="form.status" class="control">
              <option>待评估</option>
              <option>编标中</option>
              <option>审标中</option>
              <option>已提交</option>
              <option>已归档</option>
            </select>
          </label>
          <label>投标结果
            <select v-model="form.result" class="control">
              <option>待定</option>
              <option>中标</option>
              <option>未中标</option>
              <option>废标</option>
            </select>
          </label>
        </div>
        <label>备注<textarea v-model="form.notes" class="control" rows="4" /></label>
        <button class="primary" :disabled="saving" @click="save">{{ saving ? '保存中...' : '保存归档' }}</button>
      </div>

      <div class="panel">
        <h3>项目列表</h3>
        <table class="data-table clickable">
          <thead><tr><th>项目</th><th>状态</th><th>结果</th><th>投标金额</th></tr></thead>
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
      <h3>归档详情：{{ archive.project.projectName }}</h3>
      <div class="detail-grid">
        <span>招标编号</span><strong>{{ archive.project.tenderNo || '-' }}</strong>
        <span>招标人</span><strong>{{ archive.project.tenderer || '-' }}</strong>
        <span>项目状态</span><strong>{{ archive.project.status || '-' }}</strong>
        <span>审标报告</span><strong>{{ archive.reviews.length }}</strong>
        <span>编标记录</span><strong>{{ archive.drafts.length }}</strong>
        <span>投标结果</span><strong>{{ archive.project.result || '-' }}</strong>
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
const form = reactive<BidProject>({ projectName: '', status: '待评估', result: '待定' });

async function load() {
  projects.value = await api.listProjects();
}

async function save() {
  if (!form.projectName) {
    alert('请填写项目名称。');
    return;
  }
  saving.value = true;
  try {
    await api.createProject({ ...form });
    Object.assign(form, { projectName: '', tenderNo: '', tenderer: '', industry: '', region: '', budgetAmount: undefined, bidAmount: undefined, status: '待评估', result: '待定', notes: '' });
    await load();
    alert('归档保存成功。');
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
