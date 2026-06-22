<template>
  <section>
    <div class="page-title">
      <div>
        <h2>AI编标</h2>
        <p>根据招标要求和企业知识库素材，生成可编辑的投标文件章节初稿。</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <label>标书标题<input v-model="form.title" class="control" /></label>
        <label>章节类型
          <select v-model="form.section" class="control">
            <option>商务响应</option>
            <option>技术方案</option>
            <option>实施方案</option>
            <option>售后服务</option>
            <option>偏离说明</option>
          </select>
        </label>
        <label>关联项目
          <select v-model="form.projectId" class="control">
            <option :value="undefined">不关联项目</option>
            <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.projectName }}</option>
          </select>
        </label>
        <label>招标要求<textarea v-model="form.tenderText" class="control" rows="7" /></label>
        <label>企业知识上下文<textarea v-model="form.knowledgeContext" class="control" rows="5" /></label>
        <label>补充要求<textarea v-model="form.userRequirement" class="control" rows="3" /></label>
        <button class="primary" :disabled="loading" @click="submit">{{ loading ? '生成中...' : '生成标书' }}</button>
      </div>
      <div class="panel">
        <h3>编标结果</h3>
        <p v-if="!result" class="empty">暂无生成内容。</p>
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
  title: '投标文件初稿',
  section: '技术方案',
  tenderText: '',
  knowledgeContext: '',
  userRequirement: '',
});

async function submit() {
  if (!form.title || !form.tenderText) {
    alert('请填写标书标题和招标要求。');
    return;
  }
  loading.value = true;
  try {
    result.value = await api.draft({ ...form });
    alert('标书初稿已生成并归档。');
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  projects.value = await api.listProjects();
});
</script>
