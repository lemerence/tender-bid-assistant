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
        <label>招标文件附件
          <input class="control" type="file" multiple @change="onTenderFilesChange" />
        </label>
        <div v-if="tenderFiles.length" class="file-list">
          <span v-for="file in tenderFiles" :key="file.name">{{ file.name }}</span>
        </div>
        <label>企业知识上下文<textarea v-model="form.knowledgeContext" class="control" rows="5" /></label>
        <label>企业素材附件
          <input class="control" type="file" multiple @change="onMaterialFilesChange" />
        </label>
        <div v-if="materialFiles.length" class="file-list">
          <span v-for="file in materialFiles" :key="file.name">{{ file.name }}</span>
        </div>
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
const tenderFiles = ref<File[]>([]);
const materialFiles = ref<File[]>([]);
const form = reactive({
  projectId: undefined as number | undefined,
  title: '投标文件初稿',
  section: '技术方案',
  tenderText: '',
  knowledgeContext: '',
  userRequirement: '',
});

/** 校验编标输入，按是否选择附件决定 JSON 或 multipart 请求方式。 */
async function submit() {
  if (!form.title || !form.tenderText) {
    alert('请填写标书标题和招标要求。');
    return;
  }
  loading.value = true;
  try {
    // 没有附件时使用轻量 JSON 接口；存在任一附件时统一由 multipart 接口归档。
    if (tenderFiles.value.length || materialFiles.value.length) {
      result.value = await api.draftWithFiles({ ...form }, tenderFiles.value, materialFiles.value);
    } else {
      result.value = await api.draft({ ...form });
    }
    alert('标书初稿已生成并归档。');
  } finally {
    loading.value = false;
  }
}

/** 将招标文件输入框中的 FileList 转换为可响应的普通数组。 */
function onTenderFilesChange(event: Event) {
  const input = event.target as HTMLInputElement;
  tenderFiles.value = Array.from(input.files || []);
}

/** 将企业素材输入框中的 FileList 转换为可响应的普通数组。 */
function onMaterialFilesChange(event: Event) {
  const input = event.target as HTMLInputElement;
  materialFiles.value = Array.from(input.files || []);
}

// 关联项目是可选项，页面初始化时预加载项目下拉列表。
onMounted(async () => {
  projects.value = await api.listProjects();
});
</script>
