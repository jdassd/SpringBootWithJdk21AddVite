<template>
  <nav class="admin-nav">
    <div class="admin-nav-header">
      <div>
        <p class="admin-nav-title">管理后台</p>
        <span class="admin-nav-subtitle">内容与配置中心</span>
      </div>
    </div>
    <el-menu :default-active="activeKey" class="admin-menu" @select="handleSelect">
      <el-menu-item v-for="item in items" :key="item.key" :index="item.key">
        <el-icon v-if="item.icon">
          <component :is="item.icon" />
        </el-icon>
        <span>{{ item.label }}</span>
      </el-menu-item>
    </el-menu>
  </nav>
</template>

<script setup>
const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
  activeKey: {
    type: String,
    required: true,
  },
});

const emit = defineEmits(['select']);

const handleSelect = (key) => {
  emit('select', key);
};
</script>

<style scoped>
.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-nav-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.admin-nav-title {
  margin: 0;
  font-weight: 600;
  color: #1f2a44;
  font-size: 18px;
}

.admin-nav-subtitle {
  color: #7b849c;
  font-size: 12px;
}

.admin-menu {
  border: none;
}
</style>
