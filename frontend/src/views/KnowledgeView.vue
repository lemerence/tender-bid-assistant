<template>
  <section>
    <div class="page-title">
      <div>
        <h2>企业知识库</h2>
        <p>沉淀企业资质、人员能力、项目业绩、技术方案和售后服务素材。</p>
      </div>
      <input v-model="keyword" class="control search" placeholder="搜索标题或内容" @change="load" />
    </div>

    <div class="grid-2">
      <div class="panel">
        <h3>新增知识</h3>
        <label>标题<input v-model="form.title" class="control" /></label>
        <label>分类
          <select v-model="form.category" class="control">
            <option>企业资质</option>
            <option>人员能力</option>
            <option>项目业绩</option>
            <option>技术方案</option>
            <option>售后服务</option>
          </select>
        </label>
        <label>标签<input v-model="form.tags" class="control" placeholder="多个标签可用逗号分隔" /></label>
        <label>内容<textarea v-model="form.content" class="control" rows="8" /></label>
        <button class="primary" :disabled="saving" @click="save">{{ saving ? '保存中...' : '保存知识' }}</button>
      </div>

      <div class="panel">
        <h3>知识条目</h3>
        <table class="data-table">
          <thead><tr><th>标题</th><th>分类</th><th>标签</th></tr></thead>
          <tbody>
            <tr v-for="item in items" :key="item.id">
              <td>{{ item.title }}</td>
              <td>{{ item.category }}</td>
              <td>{{ item.tags || '-' }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { api, type KnowledgeItem } from '../api/client';

const keyword = ref('');
const saving = ref(false);
const items = ref<KnowledgeItem[]>([]);
const form = reactive<KnowledgeItem>({ title: '', category: '企业资质', tags: '', content: '' });

async function load() {
  items.value = await api.listKnowledge(keyword.value);
}

async function save() {
  if (!form.title || !form.content) {
    alert('请填写标题和内容。');
    return;
  }
  saving.value = true;
  try {
    await api.createKnowledge({ ...form });
    Object.assign(form, { title: '', category: '企业资质', tags: '', content: '' });
    await load();
    alert('保存成功。');
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>
