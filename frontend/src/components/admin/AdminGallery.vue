<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>摄影专辑</h3>
        <p>管理摄影专辑、描述与封面。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增专辑</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="albums" style="width: 100%" v-loading="loading">
          <el-table-column prop="title" label="标题" min-width="160" />
          <el-table-column prop="description" label="描述" min-width="200" />
          <el-table-column prop="coverUrl" label="封面地址" min-width="200" />
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="!isMobile" class="admin-module-form">
        <AdminGalleryForm
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
      <AdminGalleryForm
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

const albums = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const formModel = ref({
  title: '',
  description: '',
  coverUrl: '',
});

const rules = {
  title: [
    { required: true, message: '请输入专辑标题', trigger: 'blur' },
    { min: 2, max: 60, message: '标题长度为 2-60 个字符', trigger: 'blur' },
  ],
  description: [{ max: 120, message: '描述不超过 120 个字符', trigger: 'blur' }],
  coverUrl: [
    {
      pattern: /^(https?:\/\/)?\S*$/i,
      message: '请输入有效的封面地址',
      trigger: 'blur',
    },
  ],
};

const fetchAlbums = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/gallery/albums');
    albums.value = response.data;
  } catch (error) {
    notifyError('加载专辑失败');
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
    description: row.description || '',
    coverUrl: row.coverUrl || '',
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
  formModel.value = { title: '', description: '', coverUrl: '' };
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    try {
      if (editingId.value) {
        await axios.put(`/api/gallery/albums/${editingId.value}`, formModel.value);
        notifySuccess('专辑已更新');
      } else {
        await axios.post('/api/gallery/albums', formModel.value);
        notifySuccess('专辑已新增');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchAlbums();
    } catch (error) {
      notifyError('保存专辑失败');
    }
  });
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该专辑吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/gallery/albums/${row.id}`);
    notifySuccess('专辑已删除');
    await fetchAlbums();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除专辑失败');
    }
  }
};

const AdminGalleryForm = {
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
      <h4>{{ isEditing ? '编辑专辑' : '新增专辑' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="输入专辑标题" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="输入专辑描述" />
        </el-form-item>
        <el-form-item label="封面地址" prop="coverUrl">
          <el-input v-model="form.coverUrl" placeholder="https://" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '新增专辑' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(fetchAlbums);
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
