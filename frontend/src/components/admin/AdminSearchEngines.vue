<template>
  <section class="admin-module">
    <div class="admin-module-header">
      <div>
        <h3>搜索引擎</h3>
        <p>配置搜索引擎与默认项。</p>
      </div>
      <el-button type="primary" @click="openCreate">新增引擎</el-button>
    </div>

    <div class="admin-module-grid">
      <el-card class="admin-module-list">
        <el-table :data="engines" style="width: 100%" v-loading="loading">
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="queryUrl" label="查询地址" min-width="220" />
          <el-table-column prop="isDefault" label="默认" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.isDefault ? 'success' : 'info'">
                {{ scope.row.isDefault ? '是' : '否' }}
              </el-tag>
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
        <AdminSearchForm
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
      <AdminSearchForm
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

const engines = ref([]);
const loading = ref(false);
const editingId = ref(null);
const drawerVisible = ref(false);
const formRef = ref(null);
const drawerFormRef = ref(null);

const formModel = ref({
  name: '',
  queryUrl: '',
  isDefault: false,
});

const rules = {
  name: [
    { required: true, message: '请输入名称', trigger: 'blur' },
    { min: 2, max: 30, message: '名称长度为 2-30 个字符', trigger: 'blur' },
  ],
  queryUrl: [
    { required: true, message: '请输入查询地址', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value || !value.includes('{query}')) {
          callback(new Error('查询地址需包含 {query} 参数'));
        } else {
          callback();
        }
      },
      trigger: 'blur',
    },
  ],
};

const fetchEngines = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/search-engines');
    engines.value = response.data;
  } catch (error) {
    notifyError('加载搜索引擎失败');
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
    queryUrl: row.queryUrl || '',
    isDefault: Boolean(row.isDefault),
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
  formModel.value = { name: '', queryUrl: '', isDefault: false };
};

const submit = async (formEl) => {
  if (!formEl) return;
  await formEl.validate(async (valid) => {
    if (!valid) return;
    try {
      if (editingId.value) {
        await axios.put(`/api/search-engines/${editingId.value}`, formModel.value);
        notifySuccess('搜索引擎已更新');
      } else {
        await axios.post('/api/search-engines', formModel.value);
        notifySuccess('搜索引擎已新增');
      }
      drawerVisible.value = false;
      resetForm();
      await fetchEngines();
    } catch (error) {
      notifyError('保存搜索引擎失败');
    }
  });
};

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除该搜索引擎吗？', '删除确认', {
      type: 'warning',
    });
    await axios.delete(`/api/search-engines/${row.id}`);
    notifySuccess('搜索引擎已删除');
    await fetchEngines();
  } catch (error) {
    if (error !== 'cancel') {
      notifyError('删除搜索引擎失败');
    }
  }
};

const AdminSearchForm = {
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
      <h4>{{ isEditing ? '编辑引擎' : '新增引擎' }}</h4>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="输入搜索引擎名称" />
        </el-form-item>
        <el-form-item label="查询地址" prop="queryUrl">
          <el-input v-model="form.queryUrl" placeholder="https://...{query}" />
        </el-form-item>
        <el-form-item label="设为默认" prop="isDefault">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
        <div class="form-actions">
          <el-button type="primary" @click="submitForm($refs.formRef)">
            {{ isEditing ? '保存修改' : '新增引擎' }}
          </el-button>
          <el-button @click="reset">重置</el-button>
        </div>
      </el-form>
    </div>
  `,
};

onMounted(fetchEngines);
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
