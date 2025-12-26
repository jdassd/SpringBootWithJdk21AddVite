<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>用户管理</h3>
        <p>系统账户概览，支持角色授权与账户维护。</p>
      </div>
    </div>

    <el-card class="modern-card">
      <el-table :data="users" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.role === 'ADMIN' ? 'danger' : 'success'" effect="plain">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="管理权限" width="220">
          <template #default="scope">
            <el-button 
              v-if="scope.row.role !== 'ADMIN'" 
              size="small" 
              type="primary" 
              @click="toggleRole(scope.row, 'ADMIN')"
            >
              设为管理员
            </el-button>
            <el-button 
              v-else 
              size="small" 
              type="warning" 
              @click="toggleRole(scope.row, 'USER')"
            >
              取消管理权限
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              plain 
              @click="removeUser(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :total="pagination.total"
          layout="prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessageBox, ElMessage } from 'element-plus';

const users = ref([]);
const loading = ref(false);
const pagination = ref({ page: 1, size: 20, total: 0 });

const fetchUsers = async () => {
  loading.value = true;
  try {
    const res = await axios.get('/api/admin/users', {
      params: { page: pagination.value.page - 1, size: pagination.value.size }
    });
    users.value = res.data.items || [];
    pagination.value.total = res.data.total || 0;
  } catch (e) {
    ElMessage.error('获取用户列表失败');
  } finally {
    loading.value = false;
  }
};

const toggleRole = async (user, newRole) => {
  try {
    await ElMessageBox.confirm(
      `确定要将用户 "${user.username}" 的角色修改为 ${newRole} 吗？`,
      '角色变更确认'
    );
    await axios.patch(`/api/admin/users/${user.id}/role`, { role: newRole });
    ElMessage.success('权限更新成功');
    fetchUsers();
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('权限更新失败');
  }
};

const removeUser = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除用户 "${user.username}" 吗？此操作不可逆，该用户的所有数据都将失效。`,
      '用户删除确认',
      { type: 'warning' }
    );
    await axios.delete(`/api/admin/users/${user.id}`);
    ElMessage.success('账户已移除');
    fetchUsers();
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除用户失败');
  }
};

const formatDateTime = (val) => val ? new Date(val).toLocaleString() : '-';

const handlePageChange = (page) => {
  pagination.value.page = page;
  fetchUsers();
};

onMounted(fetchUsers);
</script>

<style scoped>
.admin-module {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.admin-module-header h3 {
  margin: 0 0 4px;
}
.pagination-bar {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
