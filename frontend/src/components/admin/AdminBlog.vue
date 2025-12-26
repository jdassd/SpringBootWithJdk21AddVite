<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>博客文章</h3>
        <p>维护文章内容、标签与发布状态。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增文章</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="posts" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="status" label="状态" width="100" />
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
        <AdminBlogForm
          ref="formRef"
          :form="formModel"
          :rules="rules"
          :tag-options="tagOptions"
          :is-editing="Boolean(editingId)"
          @submit="submit"
          @reset="resetForm"
        />
      </el-card>
    </div>

    <el-drawer v-model="drawerVisible" size="90%" direction="btt" :with-header="false">
      <div class="drawer-inner" style="padding: 20px">
        <AdminBlogForm
          ref="drawerFormRef"
          :form="formModel"
          :rules="rules"
          :tag-options="tagOptions"
          :is-editing="Boolean(editingId)"
          @submit="submit"
          @reset="resetForm"
        />
      </div>
    </el-drawer>
  </section>
</template>

<script setup>
import { nextTick, onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessageBox } from 'element-plus';
import { notifyError, notifySuccess } from '../../utils/notify';
import MarkdownIt from 'markdown-it';

const props = defineProps({
  isMobile: {
    type: Boolean,
    default: false,
  },
});

const posts = ref([]);
const tagOptions = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const md = new MarkdownIt();

const formModel = ref({
  title: '',
  content: '',
  visibility: 'DRAFT',
  publishedAt: '',
  tags: [],
});

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 80, message: '标题长度为 2-80 个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入正文内容', trigger: 'blur' },
    { min: 10, message: '正文内容不少于 10 个字符', trigger: 'blur' },
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
};
rules.visibility = rules.status;
delete rules.status;

const fetchPosts = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/blog/posts');
    posts.value = response.data.items || [];
  } catch (error) {
    notifyError('加载文章失败');
  } finally {
    loading.value = false;
  }
};

const fetchTags = async () => {
  try {
    const response = await axios.get('/api/blog/tags');
    tagOptions.value = response.data.map((tag) => tag.name);
  } catch (error) {
    tagOptions.value = [];
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
    content: row.content || '',
    visibility: row.visibility || 'DRAFT',
    publishedAt: row.publishedAt || '',
    tags: row.tags || [],
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
  formModel.value = { title: '', content: '', visibility: 'DRAFT', publishedAt: '', tags: [] };
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    try {
      if (editingId.value) {
        await axios.put(`/api/blog/posts/${editingId.value}`, formModel.value);
        notifySuccess('文章已更新');
      } else {
        await axios.post('/api/blog/posts', formModel.value);
        notifySuccess('文章已新增');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchPosts();
      await fetchTags();
    } catch (error) {
      notifyError('保存文章失败');
    }
  });
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该文章吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/blog/posts/${row.id}`);
    notifySuccess('文章已删除');
    await fetchPosts();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除文章失败');
    }
  }
};

const formatDateTime = (value) => {
  if (!value) return null;
  return new Date(value).toLocaleString();
};

const AdminBlogForm = {
  props: {
    form: { type: Object, required: true },
    rules: { type: Object, required: true },
    tagOptions: { type: Array, default: () => [] },
    isEditing: { type: Boolean, default: false },
  },
  emits: ['submit', 'reset'],
  setup(props, { emit }) {
    const activeTab = ref('edit');
    const submitForm = (formRef) => emit('submit', formRef);
    const reset = () => emit('reset');
    const previewContent = computed(() => md.render(props.form.content || ''));
    return { submitForm, reset, activeTab, previewContent };
  },
  template: `
    <div class="admin-form">
      <h4>{{ isEditing ? '编辑文章' : '新增文章' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="输入文章标题" />
        </el-form-item>
        
        <el-tabs v-model="activeTab" class="editor-tabs">
          <el-tab-pane label="正文编辑 (Markdown)" name="edit">
            <el-form-item prop="content">
              <el-input v-model="form.content" type="textarea" :rows="12" placeholder="支持 Markdown 语法" />
            </el-form-item>
          </el-tab-pane>
          <el-tab-pane label="预览" name="preview">
            <div class="markdown-preview" v-html="previewContent"></div>
          </el-tab-pane>
        </el-tabs>

        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
        </el-form-item>
        
        <div class="form-actions" style="margin-top: 20px">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '新增文章' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(async () => {
  await fetchPosts();
  await fetchTags();
});
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
}

.admin-module-grid {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 420px;
  gap: 16px;
}

.markdown-preview {
  padding: 12px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: #fdfdfd;
  min-height: 275px;
  max-height: 400px;
  overflow-y: auto;
}

:deep(.markdown-preview) img {
  max-width: 100%;
}

@media (max-width: 900px) {
  .admin-module-grid {
    grid-template-columns: 1fr;
  }
}
</style>
