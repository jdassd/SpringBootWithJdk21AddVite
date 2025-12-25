<template>
  <header class="header">
    <div class="search-section">
      <el-input
        v-if="showSearch"
        v-model="searchQuery"
        placeholder="快速搜索..."
        class="global-search"
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-select v-model="selectedEngine" style="width: 100px">
            <el-option
              v-for="engine in searchEngines"
              :key="engine.id"
              :label="engine.name"
              :value="engine"
            />
          </el-select>
        </template>
      </el-input>
    </div>

    <div class="user-section">
      <div v-if="authToken" class="user-info">
        <el-dropdown trigger="click">
          <div class="avatar-wrapper">
            <el-avatar :size="32" icon="UserFilled" />
            <span class="username">{{ username }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="$emit('nav', 'profile')">个人中心</el-dropdown-item>
              <el-dropdown-item divided @click="$emit('logout')">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div v-else class="auth-buttons">
        <el-button type="primary" plain size="small" @click="$emit('openAuth', 'login')">登录</el-button>
        <el-button type="primary" size="small" @click="$emit('openAuth', 'register')">注册</el-button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, watch } from 'vue';
import { Search, UserFilled } from '@element-plus/icons-vue';

const props = defineProps({
  authToken: String,
  username: String,
  avatarUrl: String,
  searchEngines: Array,
  defaultEngine: Object,
  showSearch: Boolean
});

const emit = defineEmits(['search', 'logout', 'openAuth', 'nav']);

const searchQuery = ref('');
const selectedEngine = ref(props.defaultEngine);

watch(() => props.defaultEngine, (val) => {
  if (val) selectedEngine.value = val;
}, { immediate: true });

const handleSearch = () => {
  emit('search', { query: searchQuery.value, engine: selectedEngine.value });
};
</script>

<style scoped>
.header {
  height: var(--header-height);
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-color);
  padding: 0 24px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 10;
}

.search-section {
  flex: 1;
  max-width: 600px;
}

.global-search :deep(.el-input__wrapper) {
  border-radius: var(--radius-md) 0 0 var(--radius-md);
  background: var(--bg-color);
  box-shadow: none !important;
  border: 1px solid var(--border-color);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--radius-md);
  transition: var(--transition-fast);
}

.avatar-wrapper:hover {
  background: var(--bg-color);
}

.username {
  font-weight: 500;
  color: var(--text-main);
}
</style>
