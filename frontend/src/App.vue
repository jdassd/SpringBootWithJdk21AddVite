<template>
  <el-container class="layout-container">
    <Sidebar 
      :active-view="currentView" 
      :is-admin="isAdmin"
      @update:view="switchView" 
    />
    
    <el-container direction="vertical">
      <Header 
        :auth-token="authToken"
        :username="username"
        :search-engines="searchEngines"
        :default-engine="selectedEngine"
        :show-search="currentView === 'home'"
        @search="doSearch"
        @logout="logout"
        @open-auth="openAuthDialog"
      />
      
      <el-main class="main-content">
        <transition name="fade-transform" mode="out-in">
          <div :key="currentView">
            <!-- Home View -->
            <HomeView 
              v-if="currentView === 'home'"
              :loading="loading"
              :stats="{
                links: navigationLinks.length,
                posts: blogPosts.length,
                albums: albums.length
              }"
              @open-rss="openRss"
              @refresh="refreshAll"
            />

            <!-- Other Views (Navigation, Blog, etc.) -->
            <section v-else class="content-section">
              <div class="section-header">
                <div class="header-text">
                  <h2>{{ viewTitle }}</h2>
                  <p>{{ viewDescription }}</p>
                </div>
              </div>

              <!-- Navigation -->
              <el-card v-if="currentView === 'navigation'" class="modern-card">
                <div v-if="navigationLinks.length" class="nav-grid">
                  <button
                    v-for="link in navigationLinks"
                    :key="link.id"
                    class="nav-link"
                    @click="openLink(link.url)"
                  >
                    <span class="link-name">{{ link.name }}</span>
                    <el-tag size="small" effect="plain">{{ link.groupName || '常用' }}</el-tag>
                  </button>
                </div>
                <el-empty v-else description="暂无导航链接" />
              </el-card>

              <!-- Blog -->
              <BlogView v-if="currentView === 'blog'" :posts="blogPosts" />

              <!-- Gallery -->
              <GalleryView v-if="currentView === 'gallery'" :albums="albums" />

              <!-- Tasks & Mail & Admin (Keep original logic but wrapped in modern cards) -->
              <el-card v-if="currentView === 'tasks'" class="modern-card">
                <div v-if="!authToken" class="auth-placeholder">
                  <el-result icon="warning" title="需要登录" sub-title="请先登录以管理任务与提醒">
                    <template #extra>
                      <el-button type="primary" @click="openAuthDialog('login')">去登录</el-button>
                    </template>
                  </el-result>
                </div>
                <!-- ... task content ... -->
                <div v-else class="split-layout">
                   <!-- Keep original split content here, but simplified styles -->
                   <div class="task-form-wrapper">
                     <h3>新增任务</h3>
                     <el-form :model="taskForm" label-position="top">
                       <el-form-item label="任务标题"><el-input v-model="taskForm.title" /></el-form-item>
                       <el-form-item label="截止日期"><el-date-picker v-model="taskForm.dueDate" type="date" width="100%" /></el-form-item>
                       <el-button type="primary" @click="createTask" block>保存任务</el-button>
                     </el-form>
                   </div>
                   <div class="task-list-wrapper">
                     <h3>任务列表</h3>
                     <div v-for="task in tasks" :key="task.id" class="item-row">
                       <span>{{ task.title }}</span>
                       <el-tag :type="task.status === 'COMPLETED' ? 'success' : 'info'">{{ task.status }}</el-tag>
                     </div>
                   </div>
                </div>
              </el-card>

              <AdminPanel v-if="currentView === 'admin' && isAdmin" :is-admin="isAdmin" />
            </section>
          </div>
        </transition>
      </el-main>
    </el-container>

    <!-- Auth Dialog -->
    <el-dialog v-model="authDialogVisible" :title="authTab === 'login' ? '欢迎回来' : '加入我们'" width="400px" append-to-body>
      <el-tabs v-model="authTab" @tab-change="refreshCaptcha">
        <el-tab-pane label="登录" name="login">
          <!-- Simplified login form -->
          <el-form :model="loginForm" label-position="top">
            <el-form-item label="用户名"><el-input v-model="loginForm.username" /></el-form-item>
            <el-form-item label="密码"><el-input v-model="loginForm.password" type="password" show-password /></el-form-item>
            <el-form-item label="验证码">
              <div class="captcha-box">
                <el-input v-model="loginForm.captchaCode" />
                <img :src="captchaImage(loginCaptcha)" @click="refreshCaptcha('login')" v-if="loginCaptcha" />
              </div>
            </el-form-item>
            <el-button type="primary" @click="login" style="width: 100%">立即登录</el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top">
            <el-form-item label="用户名"><el-input v-model="registerForm.username" placeholder="3-32位字符" /></el-form-item>
            <el-form-item label="电子邮箱"><el-input v-model="registerForm.email" placeholder="example@mail.com" /></el-form-item>
            <el-form-item label="设置密码"><el-input v-model="registerForm.password" type="password" show-password placeholder="至少8位" /></el-form-item>
            <el-form-item label="验证码">
              <div class="captcha-box">
                <el-input v-model="registerForm.captchaCode" />
                <img :src="captchaImage(loginCaptcha)" @click="refreshCaptcha('register')" v-if="loginCaptcha" />
              </div>
            </el-form-item>
            <el-button type="primary" @click="register" style="width: 100%">提 交 注 册</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import Sidebar from './components/layout/Sidebar.vue';
import Header from './components/layout/Header.vue';
import HomeView from './components/views/HomeView.vue';
import BlogView from './components/views/BlogView.vue';
import GalleryView from './components/views/GalleryView.vue';
import AdminPanel from './components/admin/AdminPanel.vue';

// --- State ---
const loading = ref(false);
const currentView = ref('home');
const authDialogVisible = ref(false);
const authTab = ref('login');

// Data
const navigationLinks = ref([]);
const searchEngines = ref([]);
const selectedEngine = ref(null);
const blogPosts = ref([]);
const albums = ref([]);
const tasks = ref([]);
const authToken = ref(localStorage.getItem('authToken') || '');
const userRole = ref(localStorage.getItem('userRole') || '');
const username = ref(localStorage.getItem('username') || '');

// Forms
const loginForm = ref({ username: '', password: '', captchaCode: '' });
const registerForm = ref({ username: '', email: '', password: '', captchaCode: '' });
const taskForm = ref({ title: '', dueDate: null });
const loginCaptcha = ref(null);

// --- Computed ---
const isAdmin = computed(() => userRole.value === 'ADMIN');
const viewTitle = computed(() => ({
  navigation: '导航中心',
  blog: '博客内容',
  gallery: '摄影展',
  tasks: '任务提醒',
  admin: '管理后台'
}[currentView.value] || ''));

const viewDescription = computed(() => ({
  navigation: '精选常用工具网站',
  blog: '探索最新技术文章',
  gallery: '浏览精选摄影作品',
  tasks: '管理您的每日待办'
}[currentView.value] || ''));

// --- Methods ---
const switchView = (view) => {
  if (view === 'admin' && !isAdmin.value) {
    ElMessage.warning('管理权限不足');
    return;
  }
  currentView.value = view;
};

const openAuthDialog = (tab) => {
  authTab.value = tab;
  authDialogVisible.value = true;
  refreshCaptcha(tab);
};

const updateAuthHeader = () => {
  if (authToken.value) axios.defaults.headers.common['X-Auth-Token'] = authToken.value;
  else delete axios.defaults.headers.common['X-Auth-Token'];
};

const refreshAll = async () => {
  loading.value = true;
  try {
    const [nav, eng, post, alb] = await Promise.all([
      axios.get('/api/navigation/public'),
      axios.get('/api/search-engines/public'),
      axios.get('/api/blog/posts/public'),
      axios.get('/api/gallery/public/albums'),
    ]);
    navigationLinks.value = nav.data;
    searchEngines.value = eng.data;
    blogPosts.value = post.data;
    albums.value = alb.data;
    selectedEngine.value = eng.data.find(e => e.isDefault) || eng.data[0];
  } finally {
    loading.value = false;
  }
};

const doSearch = ({ query, engine }) => {
  if (!query || !engine) return;
  const url = engine.queryUrl.replace('{query}', encodeURIComponent(query));
  window.open(url, '_blank');
};

const refreshCaptcha = async (type) => {
  const res = await axios.get('/api/auth/captcha');
  loginCaptcha.value = res.data;
};

const captchaImage = (c) => c ? `data:image/png;base64,${c.imageBase64}` : '';

const login = async () => {
  try {
    const res = await axios.post('/api/auth/login', {
      ...loginForm.value,
      captchaToken: loginCaptcha.value.token
    });
    authToken.value = res.data.token;
    username.value = res.data.username;
    userRole.value = res.data.role;
    localStorage.setItem('authToken', authToken.value);
    localStorage.setItem('userRole', userRole.value);
    localStorage.setItem('username', username.value);
    updateAuthHeader();
    authDialogVisible.value = false;
    ElMessage.success('登录成功');
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '登录失败');
    refreshCaptcha('login');
  }
};

const register = async () => {
  try {
    const res = await axios.post('/api/auth/register', {
      ...registerForm.value,
      captchaToken: loginCaptcha.value.token
    });
    authToken.value = res.data.token;
    username.value = res.data.username;
    userRole.value = res.data.role;
    localStorage.setItem('authToken', authToken.value);
    localStorage.setItem('userRole', userRole.value);
    localStorage.setItem('username', username.value);
    updateAuthHeader();
    authDialogVisible.value = false;
    ElMessage.success('注册成功并已自动登录');
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '注册失败');
    refreshCaptcha('register');
  }
};

const logout = () => {
  authToken.value = '';
  localStorage.clear();
  updateAuthHeader();
  ElMessage.info('已退出登录');
};

const openLink = (url) => window.open(url, '_blank');

onMounted(() => {
  updateAuthHeader();
  refreshAll();
});
</script>

<style>
/* Global Layout Styles */
.layout-container {
  height: 100vh;
  background: var(--bg-color);
}

.main-content {
  padding: 32px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.modern-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-md);
}

.section-header {
  margin-bottom: 32px;
}

.section-header h2 {
  font-size: 1.75rem;
  font-weight: 700;
  color: var(--text-main);
  margin-bottom: 4px;
}

.section-header p {
  color: var(--text-secondary);
}

/* Nav Grid */
.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px;
}

.nav-link {
  background: var(--bg-color);
  border: 1px solid var(--border-color);
  padding: 20px 16px;
  border-radius: var(--radius-md);
  text-align: left;
  cursor: pointer;
  transition: all var(--transition-fast);
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.nav-link:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
  transform: translateY(-2px);
}

.link-name {
  font-weight: 600;
  color: var(--text-main);
}

/* Post Grid */
.post-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.post-card h3 {
  margin-top: 0;
  color: var(--text-main);
}

/* Gallery Grid */
.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.album-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.album-info {
  padding: 16px;
}

.album-info h4 {
  margin: 0 0 8px;
}

/* Animations */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}

.captcha-box {
  display: flex;
  gap: 12px;
  align-items: center;
}

.captcha-box img {
  height: 32px;
  cursor: pointer;
  border-radius: 4px;
}

.split-layout {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 40px;
}

.item-row {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: var(--bg-color);
  border-radius: var(--radius-md);
  margin-bottom: 8px;
}
</style>
