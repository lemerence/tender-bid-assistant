<template>
  <section>
    <div class="page-title">
      <div>
        <h2>Enterprise Knowledge Base</h2>
        <p>Store qualifications, staff, project references, and reusable proposal materials.</p>
      </div>
      <input v-model="keyword" class="control search" placeholder="Search title or content" @change="load" />
    </div>

    <div class="grid-2">
      <div class="panel">
        <h3>Add Knowledge</h3>
        <label>Title<input v-model="form.title" class="control" /></label>
        <label>Category
          <select v-model="form.category" class="control">
            <option>Qualification</option>
            <option>Staff</option>
            <option>Project Reference</option>
            <option>Technical Plan</option>
            <option>After-sales Service</option>
          </select>
        </label>
        <label>Tags<input v-model="form.tags" class="control" /></label>
        <label>Content<textarea v-model="form.content" class="control" rows="8" /></label>
        <button class="primary" :disabled="saving" @click="save">{{ saving ? 'Saving...' : 'Save' }}</button>
      </div>

      <div class="panel">
        <h3>Knowledge Items</h3>
        <table class="data-table">
          <thead><tr><th>Title</th><th>Category</th><th>Tags</th></tr></thead>
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
const form = reactive<KnowledgeItem>({ title: '', category: 'Qualification', tags: '', content: '' });

async function load() {
  items.value = await api.listKnowledge(keyword.value);
}

async function save() {
  if (!form.title || !form.content) {
    alert('Please fill title and content.');
    return;
  }
  saving.value = true;
  try {
    await api.createKnowledge({ ...form });
    Object.assign(form, { title: '', category: 'Qualification', tags: '', content: '' });
    await load();
    alert('Saved.');
  } finally {
    saving.value = false;
  }
}

onMounted(load);
</script>
