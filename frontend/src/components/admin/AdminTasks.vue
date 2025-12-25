<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>任务列表</h3>
        <p>维护任务、提醒时间与完成状态。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增任务</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="tasks" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="dueDate" label="截止日期" min-width="120" />
          <el-table-column prop="reminderTime" label="提醒时间" min-width="180">
            <template #default="scope">
              {{ formatDateTime(scope.row.reminderTime) || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="110">
            <template #default="scope">
              <el-tag :type="scope.row.status === 'COMPLETED' ? 'success' : 'info'">
                {{ scope.row.status === 'COMPLETED' ? '已完成' : '进行中' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="240">
            <template #default="scope">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button
                v-if="scope.row.status !== 'COMPLETED'"
                size="small"
                type="success"
                plain
                @click="complete(scope.row)"
              >
                完成
              </el-button>
              <el-button size="small" type="danger" plain @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="!isMobile" class="admin-module-form">
        <AdminTaskForm
          ref="formRef"
          :form="formModel"
          :rules="rules"
          :is-editing="Boolean(editingId)"
          @submit="submit"
          @reset="resetForm"
        />
      </el-card>
    </div>

    <el-drawer v-model="drawerVisible" size="90%" direction="btt" :with-header="false">
      <AdminTaskForm
        ref="drawerFormRef"
        :form="formModel"
        :rules="rules"
        :is-editing="Boolean(editingId)"
        @submit="submit"
        @reset="resetForm"
      />
    </el-drawer>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessageBox } from 'element-plus';
import { notifyError, notifySuccess } from '../../utils/notify';

const props = defineProps({
  isMobile: {
    type: Boolean,
    default: false,
  },
});

const tasks = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const formModel = ref({
  title: '',
  dueDate: null,
  reminderTime: null,
});

const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 60, message: '标题长度为 2-60 个字符', trigger: 'blur' },
  ],
};

const fetchTasks = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/tasks');
    tasks.value = response.data;
  } catch (error) {
    notifyError('加载任务失败');
  } finally {
    loading.value = false;
  }
};

const openCreate = () => {
  resetForm();
  if (props.isMobile) {
    drawerVisible.value = true;
    nextTick(() => drawerFormRef.value?.clearValidate());
  } else {
    formRef.value?.clearValidate();
  }
};

const openEdit = (row) => {
  editingId.value = row.id;
  formModel.value = {
    title: row.title || '',
    dueDate: row.dueDate ? new Date(row.dueDate) : null,
    reminderTime: row.reminderTime ? new Date(row.reminderTime) : null,
  };
  if (props.isMobile) {
    drawerVisible.value = true;
  }
  nextTick(() => {
    formRef.value?.clearValidate();
    drawerFormRef.value?.clearValidate();
  });
};

const resetForm = () => {
  editingId.value = null;
  formModel.value = { title: '', dueDate: null, reminderTime: null };
};

const formatDate = (value) => {
  if (!value) return null;
  const date = new Date(value);
  return date.toISOString().split('T')[0];
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    const payload = {
      title: formModel.value.title,
      dueDate: formatDate(formModel.value.dueDate),
      reminderTime: formModel.value.reminderTime
        ? new Date(formModel.value.reminderTime).toISOString()
        : null,
    };
    try {
      if (editingId.value) {
        await axios.put(`/api/tasks/${editingId.value}`, payload);
        notifySuccess('任务已更新');
      } else {
        await axios.post('/api/tasks', payload);
        notifySuccess('任务已新增');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchTasks();
    } catch (error) {
      notifyError('保存任务失败');
    }
  });
};

const complete = async (row) => {
  try {
    await axios.post(`/api/tasks/${row.id}/complete`);
    notifySuccess('任务已完成');
    await fetchTasks();
  } catch (error) {
    notifyError('更新任务失败');
  }
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该任务吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/tasks/${row.id}`);
    notifySuccess('任务已删除');
    await fetchTasks();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除任务失败');
    }
  }
};

const formatDateTime = (value) => {
  if (!value) return null;
  return new Date(value).toLocaleString();
};

const AdminTaskForm = {
  props: {
    form: { type: Object, required: true },
    rules: { type: Object, required: true },
    isEditing: { type: Boolean, default: false },
  },
  emits: ['submit', 'reset'],
  setup(props, { emit }) {
    const submitForm = (formRef) => emit('submit', formRef);
    const reset = () => emit('reset');
    return { submitForm, reset };
  },
  template: `
    <div class="admin-form">
      <h4>{{ isEditing ? '编辑任务' : '新增任务' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="输入任务标题" />
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker v-model="form.dueDate" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="提醒时间" prop="reminderTime">
          <el-date-picker v-model="form.reminderTime" type="datetime" placeholder="选择提醒时间" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '新增任务' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(fetchTasks);
</script>

<style scoped>
.admin-module {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-module-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.admin-module-header h3 {
  margin: 0 0 4px;
  color: #1f2a44;
}

.admin-module-header p {
  margin: 0;
  color: #6b7385;
}

.admin-module-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
}

.admin-module-form {
  align-self: start;
}

.admin-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-form h4 {
  margin: 0;
  font-size: 16px;
}

.form-actions {
  display: flex;
  gap: 8px;
}

@media (max-width: 900px) {
  .admin-module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
