<template>
  <div class="page">
    <el-card class="panel">
      <template #header>
        <div class="card-header">
          <div>
            <h1>Spring Boot + Vite + Element Plus</h1>
            <p>JDK 21 / MyBatis / TkMapper demo</p>
          </div>
          <el-tag type="success">Running</el-tag>
        </div>
      </template>
      <el-table :data="users" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="Name" />
        <el-table-column prop="email" label="Email" />
      </el-table>
      <el-divider />
      <el-button type="primary" :loading="loading" @click="fetchUsers">
        Refresh Users
      </el-button>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';

const users = ref([]);
const loading = ref(false);

const fetchUsers = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/users');
    users.value = response.data;
  } finally {
    loading.value = false;
  }
};

onMounted(fetchUsers);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 48px 24px;
  background: linear-gradient(135deg, #f0f4ff, #fdf6f0);
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.panel {
  width: min(860px, 100%);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.card-header h1 {
  margin: 0 0 6px;
  font-size: 24px;
  color: #1f2a44;
}

.card-header p {
  margin: 0;
  color: #5a6475;
}
</style>
