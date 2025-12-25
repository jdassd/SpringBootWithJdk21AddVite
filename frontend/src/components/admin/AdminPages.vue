<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>自定义页面</h3>
        <p>维护页面内容与自定义样式。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增页面</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="pages" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="slug" label="Slug" min-width="140" />
          <el-table-column prop="updatedAt" label="更新时间" min-width="160">
            <template #default="scope">
              {{ formatDateTime(scope.row.updatedAt) || '-' }}
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
        <AdminPageForm
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
      <AdminPageForm
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

const pages = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const formModel = ref({
  title: '',
  slug: '',
  content: '',
  customCss: '',
});

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 60, message: '标题长度为 2-60 个字符', trigger: 'blur' },
  ],
  slug: [
    { required: true, message: '请输入页面 slug', trigger: 'blur' },
    {
      pattern: /^[a-z0-9-]+$/,
      message: 'slug 仅支持小写字母、数字与 -',
      trigger: 'blur',
    },
  ],
  content: [
    { required: true, message: '请输入页面内容', trigger: 'blur' },
    { min: 10, message: '页面内容不少于 10 个字符', trigger: 'blur' },
  ],
  customCss: [{ max: 500, message: '自定义样式不超过 500 字符', trigger: 'blur' }],
};

const fetchPages = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/pages');
    pages.value = response.data;
  } catch (error) {
    notifyError('加载页面失败');
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
    slug: row.slug || '',
    content: row.content || '',
    customCss: row.customCss || '',
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
  formModel.value = { title: '', slug: '', content: '', customCss: '' };
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    try {
      if (editingId.value) {
        await axios.put(`/api/pages/${editingId.value}`, formModel.value);
        notifySuccess('页面已更新');
      } else {
        await axios.post('/api/pages', formModel.value);
        notifySuccess('页面已新增');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchPages();
    } catch (error) {
      notifyError('保存页面失败');
    }
  });
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该页面吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/pages/${row.id}`);
    notifySuccess('页面已删除');
    await fetchPages();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除页面失败');
    }
  }
};

const formatDateTime = (value) => {
  if (!value) return null;
  return new Date(value).toLocaleString();
};

const AdminPageForm = {
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
      <h4>{{ isEditing ? '编辑页面' : '新增页面' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="输入页面标题" />
        </el-form-item>
        <el-form-item label="Slug" prop="slug">
          <el-input v-model="form.slug" placeholder="如：brand-story" />
        </el-form-item>
        <el-form-item label="页面内容" prop="content">
          <el-input v-model="form.content" type="textarea" rows="6" placeholder="输入页面内容" />
        </el-form-item>
        <el-form-item label="自定义样式" prop="customCss">
          <el-input v-model="form.customCss" type="textarea" rows="4" placeholder="可选，输入 CSS" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '新增页面' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(fetchPages);
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
  grid-template-columns: minmax(0, 1fr) 360px;
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
