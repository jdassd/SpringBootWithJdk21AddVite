<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>导航链接</h3>
        <p>管理导航链接、分组与排序。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增链接</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="links" style="width: 100%" v-loading="loading">
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="url" label="地址" min-width="220" />
          <el-table-column prop="groupName" label="分组" min-width="120" />
          <el-table-column prop="sortOrder" label="排序" width="90" />
          <el-table-column label="操作" width="160">
            <template #default="scope">
              <el-button size="small" @click="openEdit(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="remove(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card v-if="!isMobile" class="admin-module-form">
        <AdminNavigationForm
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
      <AdminNavigationForm
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

const links = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const formModel = ref({
  name: '',
  url: '',
  icon: '',
  groupName: '',
  sortOrder: 0,
});

const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 2, max: 40, message: '名称长度为 2-40 个字符', trigger: 'blur' },
  ],
  url: [
    { required: true, message: '请输入链接地址', trigger: 'blur' },
    {
      pattern: /^https?:\/\//i,
      message: '请输入以 http/https 开头的地址',
      trigger: 'blur',
    },
  ],
  groupName: [{ max: 20, message: '分组名称不超过 20 个字符', trigger: 'blur' }],
};

const fetchLinks = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/navigation');
    links.value = response.data;
  } catch (error) {
    notifyError('加载导航链接失败');
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
    name: row.name || '',
    url: row.url || '',
    icon: row.icon || '',
    groupName: row.groupName || '',
    sortOrder: row.sortOrder ?? 0,
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
  formModel.value = { name: '', url: '', icon: '', groupName: '', sortOrder: 0 };
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    try {
      if (editingId.value) {
        await axios.put(`/api/navigation/${editingId.value}`, formModel.value);
        notifySuccess('导航链接已更新');
      } else {
        await axios.post('/api/navigation', formModel.value);
        notifySuccess('导航链接已新增');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchLinks();
    } catch (error) {
      notifyError('保存导航链接失败');
    }
  });
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该导航链接吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/navigation/${row.id}`);
    notifySuccess('导航链接已删除');
    await fetchLinks();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除导航链接失败');
    }
  }
};

const AdminNavigationForm = {
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
      <h4>{{ isEditing ? '编辑链接' : '新增链接' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="示例：工作台" />
        </el-form-item>
        <el-form-item label="链接" prop="url">
          <el-input v-model="form.url" placeholder="https://" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="form.icon" placeholder="可选，图标 URL" />
        </el-form-item>
        <el-form-item label="分组" prop="groupName">
          <el-input v-model="form.groupName" placeholder="如：常用" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '新增链接' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(fetchLinks);
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
