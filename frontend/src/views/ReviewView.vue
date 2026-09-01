<template>
  <section>
    <div class="page-title">
      <div>
        <h2>AI审标</h2>
        <p>对比招标文件和已编写标书，自动识别响应缺口、风险项和修改建议。</p>
      </div>
    </div>

    <div class="grid-2">
      <div class="panel">
        <label>审查标题<input v-model="form.title" class="control" /></label>
        <label>关联项目
          <select v-model="form.projectId" class="control">
            <option :value="undefined">不关联项目</option>
            <option v-for="project in projects" :key="project.id" :value="project.id">{{ project.projectName }}</option>
          </select>
        </label>
        <label>招标文件内容<textarea v-model="form.tenderText" class="control" rows="9" /></label>
        <label>招标文件附件
          <input class="control" type="file" multiple @change="onTenderFilesChange" />
        </label>
        <div v-if="tenderFiles.length" class="file-list">
          <span v-for="file in tenderFiles" :key="file.name">{{ file.name }}</span>
        </div>
        <label>投标文件内容<textarea v-model="form.bidText" class="control" rows="9" /></label>
        <label>投标文件附件
          <input class="control" type="file" multiple @change="onBidFilesChange" />
        </label>
        <div v-if="bidFiles.length" class="file-list">
          <span v-for="file in bidFiles" :key="file.name">{{ file.name }}</span>
        </div>
        <button class="primary" :disabled="loading" @click="submit">{{ loading ? '审查中...' : '开始审标' }}</button>
      </div>

      <div class="panel">
        <h3>审标报告</h3>
        <p v-if="!result" class="empty">暂无审查结果。</p>
        <template v-else>
          <div class="alert" :class="alertType">{{ result.summary }}</div>
          <h4>问题清单</h4>
          <table class="data-table">
            <thead><tr><th>类型</th><th>严重程度</th><th>发现问题</th><th>修改建议</th></tr></thead>
            <tbody>
              <tr v-for="issue in result.issues" :key="issue.category + issue.finding">
                <td>{{ issue.category }}</td>
                <td><span :class="isHighRisk(issue.severity) ? 'issue-high' : 'issue-medium'">{{ issue.severity }}</span></td>
                <td>{{ issue.finding }}</td>
                <td>{{ issue.suggestion }}</td>
              </tr>
            </tbody>
          </table>
          <h4>提交前检查项</h4>
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
const tenderFiles = ref<File[]>([]);
const bidFiles = ref<File[]>([]);
const form = reactive({ projectId: undefined as number | undefined, title: '标书合规审查', tenderText: '', bidText: '' });

/** 兼容中英文严重程度，判断问题是否属于高风险。 */
function isHighRisk(severity: string) {
  return ['高', '致命', 'High', 'Fatal'].includes(severity);
}

/** 根据总体风险等级选择报告提示框样式。 */
const alertType = computed(() => {
  if (!result.value) return 'info';
  return isHighRisk(result.value.riskLevel) ? 'error' : result.value.riskLevel === '中' ? 'warning' : 'success';
});

/** 校验审标输入，按是否选择附件决定 JSON 或 multipart 请求方式。 */
async function submit() {
  if (!form.tenderText || !form.bidText) {
    alert('请填写招标文件内容和投标文件内容。');
    return;
  }
  loading.value = true;
  try {
    // 只要任一侧包含附件，就使用 multipart 接口统一完成报告与附件归档。
    if (tenderFiles.value.length || bidFiles.value.length) {
      result.value = await api.reviewWithFiles({ ...form }, tenderFiles.value, bidFiles.value);
    } else {
      result.value = await api.review({ ...form });
    }
    alert('审查完成，报告已归档。');
  } finally {
    loading.value = false;
  }
}

/** 读取用户选择的招标文件附件。 */
function onTenderFilesChange(event: Event) {
  const input = event.target as HTMLInputElement;
  tenderFiles.value = Array.from(input.files || []);
}

/** 读取用户选择的投标文件附件。 */
function onBidFilesChange(event: Event) {
  const input = event.target as HTMLInputElement;
  bidFiles.value = Array.from(input.files || []);
}

// 页面初始化时加载可关联的项目列表。
onMounted(async () => {
  projects.value = await api.listProjects();
});
</script>
