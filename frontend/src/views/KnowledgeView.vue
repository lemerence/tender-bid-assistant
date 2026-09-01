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
        <label>附件
          <input class="control" type="file" multiple @change="onFilesChange" />
        </label>
        <div v-if="files.length" class="file-list">
          <span v-for="file in files" :key="file.name">{{ file.name }}</span>
        </div>
        <button class="primary" :disabled="saving" @click="save">{{ saving ? '保存中...' : '保存知识' }}</button>
      </div>

      <div class="panel">
        <h3>知识条目</h3>
        <table class="data-table">
          <thead><tr><th>标题</th><th>分类</th><th>标签</th><th>附件</th></tr></thead>
          <tbody>
            <tr v-for="item in items" :key="item.id">
              <td>{{ item.title }}</td>
              <td>{{ displayCategory(item.category) }}</td>
              <td>{{ item.tags || '-' }}</td>
              <td>{{ formatAttachments(item.attachments) }}</td>
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
const files = ref<File[]>([]);
const form = reactive<KnowledgeItem>({ title: '', category: '企业资质', tags: '', content: '' });

/** 按当前关键词加载知识条目。 */
async function load() {
  items.value = await api.listKnowledge(keyword.value);
}

/** 校验并保存知识条目，按附件是否存在选择对应接口。 */
async function save() {
  if (!form.title || !form.content) {
    alert('请填写标题和内容。');
    return;
  }
  saving.value = true;
  try {
    // 有附件时使用 multipart 接口，使正文与附件在同一次业务操作中建立关联。
    if (files.value.length) {
      await api.createKnowledgeWithFiles({ ...form }, files.value);
    } else {
      await api.createKnowledge({ ...form });
    }
    Object.assign(form, { title: '', category: '企业资质', tags: '', content: '' });
    files.value = [];
    await load();
    alert('保存成功。');
  } finally {
    saving.value = false;
  }
}

/** 将文件输入框中的 FileList 转换为可响应的普通数组。 */
function onFilesChange(event: Event) {
  const input = event.target as HTMLInputElement;
  files.value = Array.from(input.files || []);
}

/** 将附件名称格式化为适合表格展示的中文顿号分隔文本。 */
function formatAttachments(attachments?: { originalFilename: string }[]) {
  if (!attachments || attachments.length === 0) return '-';
  return attachments.map((attachment) => attachment.originalFilename).join('、');
}

/** 将历史英文分类兼容映射为当前中文界面分类。 */
function displayCategory(category: string) {
  const categoryMap: Record<string, string> = {
    Qualification: '企业资质',
    Staff: '人员能力',
    'Project Reference': '项目业绩',
    'Technical Plan': '技术方案',
    'After-sales Service': '售后服务',
    company: '企业能力',
  };
  return categoryMap[category] || category;
}

// 页面首次显示时加载默认知识列表。
onMounted(load);
</script>
