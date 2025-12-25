<template>
  <section class="admin-shell">
    <header class="admin-topbar">
      <div>
        <h2>{{ activeModule?.label }}</h2>
        <p>{{ activeModule?.description }}</p>
      </div>
      <div class="admin-top-actions">
        <el-button v-if="isMobile" @click="drawerVisible = true">打开菜单</el-button>
      </div>
    </header>

    <div class="admin-body">
      <aside v-if="!isMobile" class="admin-sidebar">
        <AdminNavigation :items="modules" :active-key="activeKey" @select="selectModule" />
      </aside>
      <el-drawer v-model="drawerVisible" direction="ltr" size="280px" :with-header="false">
        <AdminNavigation :items="modules" :active-key="activeKey" @select="selectModule" />
      </el-drawer>

      <main class="admin-content">
        <div v-if="!isAdmin" class="admin-warning">
          当前账号没有管理权限，请使用管理员账号登录。
        </div>
        <component
          v-else
          :is="activeModule?.component"
          :is-mobile="isMobile"
        />
      </main>
    </div>
  </section>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import {
  CollectionTag,
  Document,
  Link,
  List,
  Message,
  Picture,
  Search,
} from '@element-plus/icons-vue';
import AdminNavigation from './AdminNavigation.vue';
import AdminBlog from './AdminBlog.vue';
import AdminNavigationLinks from './AdminNavigationLinks.vue';
import AdminPages from './AdminPages.vue';
import AdminGallery from './AdminGallery.vue';
import AdminSearchEngines from './AdminSearchEngines.vue';
import AdminTasks from './AdminTasks.vue';
import AdminMail from './AdminMail.vue';

const props = defineProps({
  isAdmin: {
    type: Boolean,
    default: false,
  },
});

const activeKey = ref('blog');
const drawerVisible = ref(false);
const isMobile = ref(false);

const modules = [
  {
    key: 'blog',
    label: '博客管理',
    description: '维护文章内容、状态与标签。',
    icon: Document,
    component: AdminBlog,
  },
  {
    key: 'navigation',
    label: '导航管理',
    description: '编辑站点导航链接与分组。',
    icon: Link,
    component: AdminNavigationLinks,
  },
  {
    key: 'pages',
    label: '页面管理',
    description: '维护自定义页面内容与样式。',
    icon: CollectionTag,
    component: AdminPages,
  },
  {
    key: 'gallery',
    label: '摄影管理',
    description: '维护摄影专辑与封面信息。',
    icon: Picture,
    component: AdminGallery,
  },
  {
    key: 'search',
    label: '搜索引擎管理',
    description: '配置搜索引擎与默认项。',
    icon: Search,
    component: AdminSearchEngines,
  },
  {
    key: 'tasks',
    label: '任务管理',
    description: '更新任务列表与提醒。',
    icon: List,
    component: AdminTasks,
  },
  {
    key: 'mail',
    label: '邮件管理',
    description: '维护邮件记录与发送配置。',
    icon: Message,
    component: AdminMail,
  },
];

const activeModule = computed(() => modules.find((item) => item.key === activeKey.value));

const selectModule = (key) => {
  activeKey.value = key;
  drawerVisible.value = false;
};

const handleResize = () => {
  isMobile.value = window.innerWidth <= 900;
};

onMounted(() => {
  handleResize();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.admin-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-topbar {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
}

.admin-topbar h2 {
  margin: 0 0 6px;
  color: #1f2a44;
  font-size: 22px;
}

.admin-topbar p {
  margin: 0;
  color: #6b7385;
}

.admin-body {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 20px;
}

.admin-sidebar {
  background: #ffffff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 12px 30px rgba(30, 41, 59, 0.08);
  height: fit-content;
  position: sticky;
  top: 24px;
}

.admin-content {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 12px 30px rgba(30, 41, 59, 0.08);
}

.admin-warning {
  background: #fff6f6;
  border-radius: 12px;
  padding: 16px;
  color: #b23b3b;
  font-weight: 500;
}

@media (max-width: 900px) {
  .admin-body {
    grid-template-columns: 1fr;
  }

  .admin-content {
    padding: 16px;
  }
}
</style>
