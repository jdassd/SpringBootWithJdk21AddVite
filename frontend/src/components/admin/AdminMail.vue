<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>邮件记录</h3>
        <p>发送邮件并维护历史记录。</p>
      </div>
      <el-button type="primary" @click="openCreate">发送邮件</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="messages" style="width: 100%" v-loading="loading">
          <el-table-column prop="subject" label="主题" min-width="160" />
          <el-table-column label="收发" min-width="200">
            <template #default="scope">
              {{ scope.row.fromAddress }} → {{ scope.row.toAddress }}
            </template>
          </el-table-column>
          <el-table-column prop="direction" label="方向" width="90">
            <template #default="scope">
              <el-tag :type="scope.row.direction === 'INBOUND' ? 'success' : 'warning'">
                {{ scope.row.direction === 'INBOUND' ? '收件' : '发件' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="时间" min-width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.createdAt) || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="!isMobile" class="admin-module-form">
        <AdminMailForm
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
      <AdminMailForm
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

const messages = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const formModel = ref({
  fromAddress: '',
  toAddress: '',
  subject: '',
  body: '',
});

const rules = {
  fromAddress: [
    { required: true, message: '请输入发件人邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  toAddress: [
    { required: true, message: '请输入收件人邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  subject: [
    { required: true, message: '请输入主题', trigger: 'blur' },
    { min: 2, max: 80, message: '主题长度为 2-80 个字符', trigger: 'blur' },
  ],
  body: [
    { required: true, message: '请输入正文内容', trigger: 'blur' },
    { min: 5, message: '正文内容不少于 5 个字符', trigger: 'blur' },
  ],
};

const fetchMessages = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/mail');
    messages.value = response.data;
  } catch (error) {
    notifyError('加载邮件列表失败');
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
    fromAddress: row.fromAddress || '',
    toAddress: row.toAddress || '',
    subject: row.subject || '',
    body: row.body || '',
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
  formModel.value = { fromAddress: '', toAddress: '', subject: '', body: '' };
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    try {
      if (editingId.value) {
        await axios.put(`/api/mail/${editingId.value}`, formModel.value);
        notifySuccess('邮件记录已更新');
      } else {
        await axios.post('/api/mail/send', formModel.value);
        notifySuccess('邮件已发送');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchMessages();
    } catch (error) {
      notifyError('保存邮件失败');
    }
  });
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该邮件记录吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/mail/${row.id}`);
    notifySuccess('邮件记录已删除');
    await fetchMessages();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除邮件失败');
    }
  }
};

const formatDateTime = (value) => {
  if (!value) return null;
  return new Date(value).toLocaleString();
};

const AdminMailForm = {
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
      <h4>{{ isEditing ? '编辑邮件' : '发送邮件' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="发件人" prop="fromAddress">
          <el-input v-model="form.fromAddress" placeholder="example@domain.com" />
        </el-form-item>
        <el-form-item label="收件人" prop="toAddress">
          <el-input v-model="form.toAddress" placeholder="example@domain.com" />
        </el-form-item>
        <el-form-item label="主题" prop="subject">
          <el-input v-model="form.subject" placeholder="输入邮件主题" />
        </el-form-item>
        <el-form-item label="正文" prop="body">
          <el-input v-model="form.body" type="textarea" rows="6" placeholder="输入邮件正文" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '发送邮件' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(fetchMessages);
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
