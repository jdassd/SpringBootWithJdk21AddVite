<template>
  <el-aside class="sidebar" width="var(--sidebar-width)">
    <div class="logo">
      <el-icon><Monitor /></el-icon>
      <span>全能盒子</span>
    </div>
    <el-menu
      :default-active="activeView"
      class="side-menu"
      @select="handleSelect"
      background-color="transparent"
      text-color="var(--sidebar-text)"
      active-text-color="var(--sidebar-active)"
    >
      <el-menu-item index="home">
        <el-icon><HomeFilled /></el-icon>
        <span>主页</span>
      </el-menu-item>
      <el-menu-item index="navigation">
        <el-icon><Position /></el-icon>
        <span>导航中心</span>
      </el-menu-item>
      <el-menu-item index="blog">
        <el-icon><Document /></el-icon>
        <span>博客内容</span>
      </el-menu-item>
      <el-menu-item index="gallery">
        <el-icon><Picture /></el-icon>
        <span>摄影展</span>
      </el-menu-item>
      <el-menu-item index="tasks">
        <el-icon><Ticket /></el-icon>
        <span>任务提醒</span>
      </el-menu-item>
      <el-menu-item index="mail">
        <el-icon><Message /></el-icon>
        <span>邮件中心</span>
      </el-menu-item>
      <el-divider v-if="isAdmin" />
      <el-menu-item v-if="isAdmin" index="admin">
        <el-icon><Setting /></el-icon>
        <span>管理后台</span>
      </el-menu-item>
    </el-menu>
  </el-aside>
</template>

<script setup>
import { 
  HomeFilled, 
  Position, 
  Document, 
  Picture, 
  Ticket, 
  Message, 
  Setting,
  Monitor
} from '@element-plus/icons-vue';

defineProps({
  activeView: String,
  isAdmin: Boolean
});

const emit = defineEmits(['update:view']);

const handleSelect = (index) => {
  emit('update:view', index);
};
</script>

<style scoped>
.sidebar {
  background-color: var(--sidebar-bg);
  height: 100vh;
  display: flex;
  flex-direction: column;
  transition: var(--transition-normal);
  border-right: 1px solid var(--border-color);
}

.logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  padding: 0 24px;
  gap: 12px;
  color: #fff;
  font-size: 1.25rem;
  font-weight: 700;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.side-menu {
  border-right: none;
  padding: 12px 16px;
}

:deep(.el-menu-item) {
  border-radius: var(--radius-md);
  margin-bottom: 4px;
  height: 48px;
}

:deep(.el-menu-item.is-active) {
  background-color: var(--primary-color) !important;
}

:deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.05);
}
</style>
