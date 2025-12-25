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
        <el-card class="admin-guide">
          <div class="admin-guide-header">
            <div>
              <h3>操作指南</h3>
              <p>快速了解管理模块用途与影响范围，完成基础配置后再发布到前台。</p>
            </div>
            <el-button
              v-if="isAdmin"
              type="primary"
              :loading="seeding"
              @click="seedDemoData"
            >
              新建示例数据
            </el-button>
          </div>
          <div class="admin-guide-grid">
            <div v-for="module in modules" :key="module.key" class="admin-guide-item">
              <h4>{{ module.label }}</h4>
              <p>用途：{{ module.purpose }}</p>
              <p>影响范围：{{ module.scope }}</p>
            </div>
          </div>
        </el-card>
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
import axios from 'axios';
import {
  CollectionTag,
  Document,
  Link,
  List,
  Message,
  Picture,
  Search,
} from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
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
const seeding = ref(false);

const modules = [
  {
    key: 'blog',
    label: '博客管理',
    description: '维护文章内容、状态与标签。',
    purpose: '规划文章内容、发布节奏与标签体系。',
    scope: '影响博客列表、详情页与 RSS 订阅。',
    icon: Document,
    component: AdminBlog,
  },
  {
    key: 'navigation',
    label: '导航管理',
    description: '编辑站点导航链接与分组。',
    purpose: '维护常用站点入口与分组排序。',
    scope: '影响首页导航中心的快捷入口。',
    icon: Link,
    component: AdminNavigationLinks,
  },
  {
    key: 'pages',
    label: '页面管理',
    description: '维护自定义页面内容与样式。',
    purpose: '创建品牌介绍、运营活动等独立页面。',
    scope: '影响自定义页面列表与前台展示。',
    icon: CollectionTag,
    component: AdminPages,
  },
  {
    key: 'gallery',
    label: '摄影管理',
    description: '维护摄影专辑与封面信息。',
    purpose: '整理摄影专辑、封面与简介。',
    scope: '影响摄影展页面与首页统计。',
    icon: Picture,
    component: AdminGallery,
  },
  {
    key: 'search',
    label: '搜索引擎管理',
    description: '配置搜索引擎与默认项。',
    purpose: '配置搜索引擎入口与默认搜索。',
    scope: '影响首页搜索框与引擎跳转。',
    icon: Search,
    component: AdminSearchEngines,
  },
  {
    key: 'tasks',
    label: '任务管理',
    description: '更新任务列表与提醒。',
    purpose: '记录待办、提醒时间与完成状态。',
    scope: '影响任务提醒模块与统计。',
    icon: List,
    component: AdminTasks,
  },
  {
    key: 'mail',
    label: '邮件管理',
    description: '维护邮件记录与发送配置。',
    purpose: '追踪邮件收发与模板内容。',
    scope: '影响邮件中心列表与通知记录。',
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

const seedDemoData = async () => {
  if (seeding.value) return;
  seeding.value = true;
  const now = new Date();
  const formatDate = (value) => value.toISOString().split('T')[0];
  const requests = [
    axios.post('/api/blog/posts', {
      title: '平台更新速报：功能中心上线',
      content: '这是一条示例文章，用于展示博客管理功能与前台展示效果。',
      status: 'PUBLISHED',
      tags: ['公告', '更新'],
    }),
    axios.post('/api/navigation', {
      name: '团队知识库',
      url: 'https://example.com/wiki',
      icon: '',
      groupName: '运营常用',
      sortOrder: 1,
    }),
    axios.post('/api/pages', {
      title: '品牌介绍',
      slug: 'brand-intro',
      content: '这是一个示例自定义页面，用于展示品牌故事与服务优势。',
      customCss: 'h1 { color: #4255d6; }',
    }),
    axios.post('/api/gallery/albums', {
      title: '城市光影',
      description: '夜景与城市人文摄影集。',
      coverUrl: 'https://images.unsplash.com/photo-1469474968028-56623f02e42e',
    }),
    axios.post('/api/search-engines', {
      name: '示例搜索',
      queryUrl: 'https://www.google.com/search?q={query}',
      isDefault: true,
    }),
    axios.post('/api/tasks', {
      title: '完善首页模块文案',
      dueDate: formatDate(now),
      reminderTime: new Date(now.getTime() + 2 * 60 * 60 * 1000).toISOString(),
    }),
    axios.post('/api/mail/send', {
      fromAddress: 'no-reply@example.com',
      toAddress: 'team@example.com',
      subject: '示例邮件：欢迎体验管理后台',
      body: '这是一封示例邮件，用于展示邮件管理模块。',
    }),
  ];

  const results = await Promise.allSettled(requests);
  const failed = results.filter((item) => item.status === 'rejected').length;
  if (failed === 0) {
    ElMessage.success('示例数据已创建');
  } else {
    ElMessage.warning(`已创建部分示例数据（失败 ${failed} 项）`);
  }
  seeding.value = false;
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
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-warning {
  background: #fff6f6;
  border-radius: 12px;
  padding: 16px;
  color: #b23b3b;
  font-weight: 500;
}

.admin-guide {
  border-radius: 16px;
  background: linear-gradient(135deg, #f7f8ff 0%, #ffffff 100%);
  border: 1px solid rgba(120, 132, 255, 0.2);
}

.admin-guide-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  margin-bottom: 16px;
}

.admin-guide-header h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #1f2a44;
}

.admin-guide-header p {
  margin: 0;
  color: #5f6b7f;
}

.admin-guide-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.admin-guide-item {
  background: #ffffff;
  border-radius: 12px;
  padding: 12px 14px;
  box-shadow: inset 0 0 0 1px rgba(230, 234, 252, 0.9);
}

.admin-guide-item h4 {
  margin: 0 0 6px;
  font-size: 15px;
  color: #1f2a44;
}

.admin-guide-item p {
  margin: 0 0 4px;
  color: #6a7387;
  font-size: 13px;
}

.admin-guide-item p:last-child {
  margin-bottom: 0;
}

@media (max-width: 900px) {
  .admin-body {
    grid-template-columns: 1fr;
  }

  .admin-content {
    padding: 16px;
  }

  .admin-guide-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
