<template>
  <div class="page">
    <header class="top-bar">
      <div>
        <h1>全能博客与导航平台</h1>
        <p>集成博客、摄影展、任务提醒与导航入口。</p>
      </div>
      <div class="auth-area">
        <div v-if="authToken" class="auth-status">
          <span>已登录：{{ username }}</span>
          <el-button size="small" @click="logout">退出登录</el-button>
        </div>
        <el-card v-else class="auth-card">
          <el-tabs v-model="authTab" @tab-change="refreshCaptcha">
            <el-tab-pane label="登录" name="login">
              <el-form :model="loginForm" label-position="top" class="auth-form">
                <el-form-item label="用户名">
                  <el-input v-model="loginForm.username" />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input v-model="loginForm.password" type="password" show-password />
                </el-form-item>
                <el-form-item label="验证码">
                  <div class="captcha-row">
                    <el-input v-model="loginForm.captchaCode" placeholder="输入验证码" />
                    <button class="captcha-button" type="button" @click="refreshCaptcha('login')">
                      <img v-if="loginCaptcha" :src="captchaImage(loginCaptcha)" alt="captcha" />
                      <span v-else>加载中</span>
                    </button>
                  </div>
                </el-form-item>
                <el-button type="primary" @click="login">登录</el-button>
              </el-form>
            </el-tab-pane>
            <el-tab-pane label="注册" name="register">
              <el-form :model="registerForm" label-position="top" class="auth-form">
                <el-form-item label="用户名">
                  <el-input v-model="registerForm.username" />
                </el-form-item>
                <el-form-item label="邮箱">
                  <el-input v-model="registerForm.email" />
                </el-form-item>
                <el-form-item label="密码">
                  <el-input v-model="registerForm.password" type="password" show-password />
                </el-form-item>
                <el-form-item label="验证码">
                  <div class="captcha-row">
                    <el-input v-model="registerForm.captchaCode" placeholder="输入验证码" />
                    <button class="captcha-button" type="button" @click="refreshCaptcha('register')">
                      <img v-if="registerCaptcha" :src="captchaImage(registerCaptcha)" alt="captcha" />
                      <span v-else>加载中</span>
                    </button>
                  </div>
                </el-form-item>
                <el-button type="primary" @click="register">注册</el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </div>
    </header>

    <header v-if="currentView === 'home'" class="hero">
      <div class="hero-content">
        <p class="hero-tag">全能博客与导航平台</p>
        <h2>现代化主页入口，功能分区更清晰</h2>
        <p class="hero-subtitle">
          首页展示功能入口，支持博客、摄影展、导航、任务提醒与邮件中心。
        </p>
        <div class="hero-actions">
          <el-button type="primary" @click="openRss">订阅 RSS</el-button>
          <el-button @click="refreshAll" :loading="loading">刷新内容</el-button>
        </div>
      </div>
      <div class="hero-panel">
        <div class="search-card">
          <h3>快速搜索</h3>
          <p>选择搜索引擎并立即跳转</p>
          <div class="search-box">
            <el-select v-model="selectedEngine" placeholder="选择引擎" class="search-select">
              <el-option
                v-for="engine in searchEngines"
                :key="engine.id"
                :label="engine.name"
                :value="engine"
              />
            </el-select>
            <el-input v-model="searchQuery" placeholder="输入关键词" class="search-input" />
            <el-button type="primary" @click="doSearch">搜索</el-button>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat">
            <span>导航链接</span>
            <strong>{{ navigationLinks.length }}</strong>
          </div>
          <div class="stat">
            <span>公开文章</span>
            <strong>{{ blogPosts.length }}</strong>
          </div>
          <div class="stat">
            <span>摄影专辑</span>
            <strong>{{ albums.length }}</strong>
          </div>
        </div>
      </div>
    </header>

    <section v-if="currentView === 'home'" class="section">
      <div class="section-header">
        <h2>功能入口</h2>
        <p>选择进入独立功能主页，避免信息堆叠。</p>
      </div>
      <div class="feature-grid">
        <el-card class="feature-card" @click="switchView('navigation')">
          <h3>导航中心</h3>
          <p>集中管理常用网站与工具。</p>
          <el-button type="primary" text>进入导航</el-button>
        </el-card>
        <el-card class="feature-card admin-entry" @click="switchView('admin')">
          <h3>管理后台</h3>
          <p>进入管理模式，集中处理全站内容。</p>
          <el-button type="primary" text>进入管理</el-button>
        </el-card>
        <el-card class="feature-card" @click="switchView('blog')">
          <h3>博客内容</h3>
          <p>查看与维护最新发布文章。</p>
          <el-button type="primary" text>进入博客</el-button>
        </el-card>
        <el-card class="feature-card" @click="switchView('gallery')">
          <h3>摄影展</h3>
          <p>浏览精选摄影专辑。</p>
          <el-button type="primary" text>进入摄影展</el-button>
        </el-card>
        <el-card class="feature-card" @click="switchView('pages')">
          <h3>自定义页面</h3>
          <p>创建品牌页与自定义入口。</p>
          <el-button type="primary" text>进入页面</el-button>
        </el-card>
        <el-card class="feature-card" @click="switchView('tasks')">
          <h3>任务提醒</h3>
          <p>每日任务管理与统计分析。</p>
          <el-button type="primary" text>进入任务</el-button>
        </el-card>
        <el-card class="feature-card" @click="switchView('mail')">
          <h3>邮件中心</h3>
          <p>收发邮件并归档历史记录。</p>
          <el-button type="primary" text>进入邮件</el-button>
        </el-card>
      </div>
    </section>

    <section v-else class="section">
      <div class="section-header with-back">
        <div>
          <h2>{{ viewTitle }}</h2>
          <p>{{ viewDescription }}</p>
        </div>
        <el-button @click="switchView('home')">返回首页</el-button>
      </div>

      <el-card v-if="currentView === 'navigation'" class="panel">
        <div v-if="navigationLinks.length" class="nav-grid">
          <button
            v-for="link in navigationLinks"
            :key="link.id"
            class="nav-link"
            @click="openLink(link.url)"
          >
            <span>{{ link.name }}</span>
            <small>{{ link.groupName || '常用' }}</small>
          </button>
        </div>
        <el-empty v-else description="暂无导航链接" />
      </el-card>

      <el-card v-if="currentView === 'blog'" class="panel">
        <div v-if="blogPosts.length" class="list">
          <div v-for="post in blogPosts" :key="post.id" class="list-item">
            <h3>{{ post.title }}</h3>
            <p>{{ post.content }}</p>
          </div>
        </div>
        <el-empty v-else description="暂无文章" />
      </el-card>

      <el-card v-if="currentView === 'gallery'" class="panel">
        <div v-if="albums.length" class="gallery">
          <div v-for="album in albums" :key="album.id" class="gallery-item">
            <img :src="album.coverUrl" :alt="album.title" />
            <div>
              <h4>{{ album.title }}</h4>
              <p>{{ album.description }}</p>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无摄影集" />
      </el-card>

      <el-card v-if="currentView === 'pages'" class="panel">
        <div class="split">
          <div>
            <h3>页面列表</h3>
            <div v-if="customPages.length" class="page-grid">
              <button
                v-for="page in customPages"
                :key="page.id"
                class="page-link"
                :class="{ active: activePage && activePage.id === page.id }"
                @click="selectPage(page)"
              >
                {{ page.title }}
              </button>
            </div>
            <el-empty v-else description="暂无自定义页面" />
          </div>
          <div v-if="activePage" class="page-preview">
            <h3>{{ activePage.title }}</h3>
            <div class="page-content" v-html="activePage.content"></div>
          </div>
          <el-empty v-else description="请选择页面预览" />
        </div>
      </el-card>

      <el-card v-if="currentView === 'tasks'" class="panel">
        <div v-if="!authToken" class="auth-warning">
          请先登录以管理任务与提醒。
        </div>
        <div v-else class="split">
          <div>
            <h3>新增任务</h3>
            <el-form :model="taskForm" label-position="top" class="task-form">
              <el-form-item label="任务标题">
                <el-input v-model="taskForm.title" />
              </el-form-item>
              <el-form-item label="截止日期">
                <el-date-picker v-model="taskForm.dueDate" type="date" placeholder="选择日期" />
              </el-form-item>
              <el-form-item label="提醒时间">
                <el-date-picker v-model="taskForm.reminderTime" type="datetime" placeholder="选择时间" />
              </el-form-item>
              <el-button type="primary" @click="createTask">保存任务</el-button>
            </el-form>
            <div class="summary-card">
              <h4>任务完成统计</h4>
              <div class="summary-controls">
                <el-select v-model="summaryPeriod" size="small" class="summary-select">
                  <el-option label="周" value="week" />
                  <el-option label="月" value="month" />
                  <el-option label="年" value="year" />
                </el-select>
                <el-date-picker v-model="summaryDate" type="date" size="small" placeholder="基准日期" />
                <el-button size="small" @click="loadTaskSummary">查询</el-button>
              </div>
              <div v-if="taskSummary" class="summary-result">
                <p>统计周期：{{ taskSummary.start }} 至 {{ taskSummary.end }}</p>
                <p>完成任务：{{ taskSummary.completed }} / {{ taskSummary.total }}</p>
              </div>
            </div>
          </div>
          <div>
            <h3>任务列表</h3>
            <div v-if="tasks.length" class="list">
              <div v-for="task in tasks" :key="task.id" class="task-item">
                <div>
                  <strong>{{ task.title }}</strong>
                  <p>截止：{{ task.dueDate || '未设置' }}</p>
                  <p>提醒：{{ task.reminderTime || '未设置' }}</p>
                </div>
                <div>
                  <el-tag :type="task.status === 'COMPLETED' ? 'success' : 'info'">
                    {{ task.status === 'COMPLETED' ? '已完成' : '进行中' }}
                  </el-tag>
                  <el-button
                    v-if="task.status !== 'COMPLETED'"
                    size="small"
                    type="primary"
                    @click="completeTask(task.id)"
                  >
                    标记完成
                  </el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无任务" />
          </div>
        </div>
      </el-card>

      <el-card v-if="currentView === 'mail'" class="panel">
        <div v-if="!authToken" class="auth-warning">
          请先登录以查看邮件。
        </div>
        <div v-else class="split">
          <div>
            <h3>发送邮件</h3>
            <el-form :model="mailForm" label-position="top" class="mail-form">
              <el-form-item label="发件人">
                <el-input v-model="mailForm.fromAddress" />
              </el-form-item>
              <el-form-item label="收件人">
                <el-input v-model="mailForm.toAddress" />
              </el-form-item>
              <el-form-item label="主题">
                <el-input v-model="mailForm.subject" />
              </el-form-item>
              <el-form-item label="正文">
                <el-input v-model="mailForm.body" type="textarea" rows="4" />
              </el-form-item>
              <div class="mail-actions">
                <el-button type="primary" @click="sendMail">发送</el-button>
                <el-button @click="receiveMail">模拟收信</el-button>
              </div>
            </el-form>
          </div>
          <div>
            <h3>邮件列表</h3>
            <div v-if="mailMessages.length" class="list">
              <div v-for="message in mailMessages" :key="message.id" class="mail-item">
                <div>
                  <strong>{{ message.subject }}</strong>
                  <p>{{ message.fromAddress }} → {{ message.toAddress }}</p>
                  <p>{{ message.body }}</p>
                </div>
                <el-tag :type="message.direction === 'INBOUND' ? 'success' : 'warning'">
                  {{ message.direction === 'INBOUND' ? '收件' : '发件' }}
                </el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无邮件" />
          </div>
        </div>
      </el-card>

      <el-card v-if="currentView === 'admin'" class="panel admin-panel">
        <div class="admin-banner">
          <div>
            <h3>管理模式</h3>
            <p>当前处于管理后台，仅限已登录用户进行内容维护。</p>
          </div>
          <el-tag type="warning" effect="dark">管理模式</el-tag>
        </div>
        <div class="admin-hint">
          所有管理接口都需要携带 <strong>X-Auth-Token</strong>，请确保登录状态有效。
        </div>
        <div class="admin-grid">
          <div v-for="item in adminModules" :key="item.title" class="admin-card">
            <div class="admin-card-header">
              <h4>{{ item.title }}</h4>
              <span>{{ item.description }}</span>
            </div>
            <div class="admin-actions">
              <el-button size="small" type="primary" plain>新增</el-button>
              <el-button size="small" plain>编辑</el-button>
              <el-button size="small" type="danger" plain>删除</el-button>
            </div>
          </div>
        </div>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const currentView = ref('home');
const navigationLinks = ref([]);
const searchEngines = ref([]);
const selectedEngine = ref(null);
const searchQuery = ref('');
const blogPosts = ref([]);
const albums = ref([]);
const customPages = ref([]);
const activePage = ref(null);
const tasks = ref([]);
const taskSummary = ref(null);
const mailMessages = ref([]);

const authToken = ref(localStorage.getItem('authToken') || '');
const username = ref(localStorage.getItem('username') || '');
const authTab = ref('login');

const loginForm = ref({
  username: '',
  password: '',
  captchaCode: '',
});

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  captchaCode: '',
});

const loginCaptcha = ref(null);
const registerCaptcha = ref(null);

const taskForm = ref({
  title: '',
  dueDate: null,
  reminderTime: null,
});

const summaryPeriod = ref('week');
const summaryDate = ref(null);

const mailForm = ref({
  fromAddress: '',
  toAddress: '',
  subject: '',
  body: '',
});

const adminModules = [
  { title: '博客管理', description: '管理博客文章、分类与发布状态。' },
  { title: '导航管理', description: '维护导航链接与分组信息。' },
  { title: '页面管理', description: '编辑自定义页面与样式。' },
  { title: '摄影管理', description: '上传与维护摄影专辑内容。' },
  { title: '搜索引擎管理', description: '配置搜索引擎列表与默认项。' },
  { title: '任务管理', description: '调整任务模板与提醒策略。' },
  { title: '邮件管理', description: '管理邮件模板与收发记录。' },
];

let customPageStyleEl;

const viewTitle = computed(() => {
  switch (currentView.value) {
    case 'navigation':
      return '导航中心';
    case 'admin':
      return '管理后台';
    case 'blog':
      return '博客内容';
    case 'gallery':
      return '摄影展';
    case 'pages':
      return '自定义页面';
    case 'tasks':
      return '任务提醒';
    case 'mail':
      return '邮件中心';
    default:
      return '功能入口';
  }
});

const viewDescription = computed(() => {
  switch (currentView.value) {
    case 'navigation':
      return '按分组查看常用站点，一键新标签打开。';
    case 'admin':
      return '进入管理后台，集中维护全站内容与配置。';
    case 'blog':
      return '浏览最新发布内容，专注阅读体验。';
    case 'gallery':
      return '查看精选摄影作品与专辑。';
    case 'pages':
      return '管理自定义页面入口与样式。';
    case 'tasks':
      return '设置每日任务提醒并跟踪完成度。';
    case 'mail':
      return '收发邮件，留存历史记录。';
    default:
      return '选择进入独立功能主页。';
  }
});

const updateAuthHeader = () => {
  if (authToken.value) {
    axios.defaults.headers.common['X-Auth-Token'] = authToken.value;
  } else {
    delete axios.defaults.headers.common['X-Auth-Token'];
  }
};

const refreshAll = async () => {
  loading.value = true;
  try {
    const [navResponse, engineResponse, postResponse, albumResponse, pageResponse] = await Promise.all([
      axios.get('/api/navigation/public'),
      axios.get('/api/search-engines/public'),
      axios.get('/api/blog/posts/public'),
      axios.get('/api/gallery/public/albums'),
      axios.get('/api/pages/public'),
    ]);
    navigationLinks.value = navResponse.data;
    searchEngines.value = engineResponse.data;
    blogPosts.value = postResponse.data;
    albums.value = albumResponse.data;
    customPages.value = pageResponse.data;
    activePage.value = customPages.value[0] || null;
    selectedEngine.value =
      searchEngines.value.find((engine) => engine.isDefault) || searchEngines.value[0] || null;
  } finally {
    loading.value = false;
  }
};

const refreshTasks = async () => {
  if (!authToken.value) {
    tasks.value = [];
    return;
  }
  const response = await axios.get('/api/tasks');
  tasks.value = response.data;
};

const refreshMail = async () => {
  if (!authToken.value) {
    mailMessages.value = [];
    return;
  }
  const response = await axios.get('/api/mail');
  mailMessages.value = response.data;
};

const switchView = (view) => {
  if (view === 'admin' && !authToken.value) {
    ElMessage.warning('请先登录后进入管理后台');
    return;
  }
  currentView.value = view;
};

const openLink = (url) => {
  window.open(url, '_blank', 'noopener,noreferrer');
};

const doSearch = () => {
  if (!selectedEngine.value || !searchQuery.value) {
    return;
  }
  const target = selectedEngine.value.queryUrl.replace('{query}', encodeURIComponent(searchQuery.value));
  openLink(target);
};

const openRss = () => {
  openLink('/api/rss');
};

const captchaImage = (captcha) => `data:image/png;base64,${captcha.imageBase64}`;

const refreshCaptcha = async (type) => {
  const response = await axios.get('/api/auth/captcha');
  if ((type || authTab.value) === 'register') {
    registerCaptcha.value = response.data;
  } else {
    loginCaptcha.value = response.data;
  }
};

const login = async () => {
  try {
    if (!loginCaptcha.value) {
      await refreshCaptcha('login');
    }
    const response = await axios.post('/api/auth/login', {
      username: loginForm.value.username,
      password: loginForm.value.password,
      captchaToken: loginCaptcha.value?.token,
      captchaCode: loginForm.value.captchaCode,
    });
    authToken.value = response.data.token;
    username.value = response.data.username;
    localStorage.setItem('authToken', authToken.value);
    localStorage.setItem('username', username.value);
    updateAuthHeader();
    await refreshTasks();
    await refreshMail();
    ElMessage.success('登录成功');
  } catch (error) {
    ElMessage.error('登录失败，请检查信息与验证码');
  } finally {
    loginForm.value.captchaCode = '';
    await refreshCaptcha('login');
  }
};

const register = async () => {
  try {
    if (!registerCaptcha.value) {
      await refreshCaptcha('register');
    }
    const response = await axios.post('/api/auth/register', {
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password,
      captchaToken: registerCaptcha.value?.token,
      captchaCode: registerForm.value.captchaCode,
    });
    authToken.value = response.data.token;
    username.value = response.data.username;
    localStorage.setItem('authToken', authToken.value);
    localStorage.setItem('username', username.value);
    updateAuthHeader();
    await refreshTasks();
    await refreshMail();
    ElMessage.success('注册成功');
  } catch (error) {
    ElMessage.error('注册失败，请检查信息与验证码');
  } finally {
    registerForm.value.captchaCode = '';
    await refreshCaptcha('register');
  }
};

const logout = async () => {
  try {
    await axios.post('/api/auth/logout');
  } finally {
    authToken.value = '';
    username.value = '';
    localStorage.removeItem('authToken');
    localStorage.removeItem('username');
    updateAuthHeader();
    tasks.value = [];
    mailMessages.value = [];
  }
};

const selectPage = (page) => {
  activePage.value = page;
};

const formatDate = (value) => {
  if (!value) return null;
  const date = new Date(value);
  return date.toISOString().split('T')[0];
};

const createTask = async () => {
  try {
    await axios.post('/api/tasks', {
      title: taskForm.value.title,
      dueDate: formatDate(taskForm.value.dueDate),
      reminderTime: taskForm.value.reminderTime ? new Date(taskForm.value.reminderTime).toISOString() : null,
    });
    taskForm.value = { title: '', dueDate: null, reminderTime: null };
    await refreshTasks();
    ElMessage.success('任务已创建');
  } catch (error) {
    ElMessage.error('创建任务失败');
  }
};

const completeTask = async (id) => {
  try {
    await axios.post(`/api/tasks/${id}/complete`);
    await refreshTasks();
    ElMessage.success('任务已完成');
  } catch (error) {
    ElMessage.error('更新任务失败');
  }
};

const loadTaskSummary = async () => {
  if (!authToken.value) return;
  const params = new URLSearchParams({ period: summaryPeriod.value });
  if (summaryDate.value) {
    params.append('date', formatDate(summaryDate.value));
  }
  const response = await axios.get(`/api/tasks/summary?${params.toString()}`);
  taskSummary.value = response.data;
};

const sendMail = async () => {
  try {
    await axios.post('/api/mail/send', mailForm.value);
    await refreshMail();
    ElMessage.success('邮件已发送');
  } catch (error) {
    ElMessage.error('邮件发送失败，请检查邮件服务配置');
  }
};

const receiveMail = async () => {
  try {
    await axios.post('/api/mail/receive', mailForm.value);
    await refreshMail();
    ElMessage.success('已记录收信');
  } catch (error) {
    ElMessage.error('记录收信失败');
  }
};

watch(currentView, async (value) => {
  if (value === 'tasks') {
    await refreshTasks();
  }
  if (value === 'mail') {
    await refreshMail();
  }
});

onMounted(async () => {
  updateAuthHeader();
  await refreshAll();
  await refreshCaptcha('login');
  await refreshCaptcha('register');
  if (authToken.value) {
    await refreshTasks();
    await refreshMail();
  }
  customPageStyleEl = document.createElement('style');
  customPageStyleEl.setAttribute('data-custom-page-style', 'true');
  customPageStyleEl.textContent = activePage.value?.customCss || '';
  document.head.appendChild(customPageStyleEl);
});

onBeforeUnmount(() => {
  if (customPageStyleEl) {
    customPageStyleEl.remove();
  }
});

watch(activePage, (page) => {
  if (customPageStyleEl) {
    customPageStyleEl.textContent = page?.customCss || '';
  }
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 24px;
  background: linear-gradient(180deg, #f5f7ff 0%, #fdf8f2 100%);
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.top-bar h1 {
  margin: 0 0 6px;
  font-size: clamp(22px, 3vw, 32px);
  color: #1f2a44;
}

.top-bar p {
  margin: 0;
  color: #6b7385;
}

.auth-area {
  display: flex;
  align-items: center;
  gap: 16px;
}

.auth-card {
  width: 320px;
}

.auth-form {
  display: grid;
  gap: 12px;
}

.auth-status {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #ffffff;
  padding: 12px 16px;
  border-radius: 16px;
  box-shadow: 0 12px 30px rgba(30, 41, 59, 0.12);
}

.captcha-row {
  display: flex;
  gap: 8px;
  align-items: center;
}

.captcha-button {
  border: none;
  background: #f6f8ff;
  padding: 4px 8px;
  border-radius: 8px;
  cursor: pointer;
}

.captcha-button img {
  height: 32px;
}

.hero {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 0.9fr);
  gap: 32px;
  align-items: center;
}

.hero-tag {
  color: #5c6b92;
  font-weight: 600;
  margin-bottom: 12px;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
}

.hero h2 {
  margin: 0 0 16px;
  font-size: clamp(26px, 3vw, 36px);
  color: #1f2a44;
}

.hero-subtitle {
  margin: 0;
  color: #5a6475;
  max-width: 560px;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.hero-content {
  display: flex;
  flex-direction: column;
}

.hero-panel {
  background: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 18px 45px rgba(30, 41, 59, 0.12);
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.search-card h3 {
  margin: 0 0 4px;
  font-size: 18px;
  color: #1f2a44;
}

.search-card p {
  margin: 0 0 16px;
  color: #7b849c;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 12px;
}

.stat {
  background: #f6f8ff;
  border-radius: 14px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat span {
  color: #7b849c;
  font-size: 12px;
}

.stat strong {
  font-size: 20px;
  color: #1f2a44;
}

.section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.panel {
  border-radius: 16px;
}

.section-header h2 {
  margin: 0 0 6px;
  font-size: 22px;
  color: #1f2a44;
}

.section-header p {
  margin: 0;
  color: #6a7387;
}

.section-header.with-back {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.feature-card {
  border-radius: 18px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.feature-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 40px rgba(30, 41, 59, 0.15);
}

.feature-card h3 {
  margin: 0 0 6px;
  font-size: 18px;
  color: #1f2a44;
}

.feature-card p {
  margin: 0 0 12px;
  color: #7b849c;
}

.admin-entry {
  border: 1px solid rgba(255, 166, 0, 0.25);
  background: linear-gradient(135deg, #fff5e6 0%, #ffffff 100%);
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.nav-link {
  border: none;
  background: #f6f8ff;
  padding: 12px;
  border-radius: 12px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.nav-link:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 18px rgba(34, 60, 80, 0.12);
}

.nav-link small {
  display: block;
  color: #7b849c;
}

.search-box {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-select {
  min-width: 140px;
}

.search-input {
  flex: 1;
  min-width: 160px;
}

.list {
  display: grid;
  gap: 12px;
}

.list-item {
  background: #f9fafc;
  padding: 12px;
  border-radius: 12px;
}

.list-item h3 {
  margin: 0 0 6px;
  font-size: 16px;
}

.list-item p {
  margin: 0;
  color: #5a6475;
  font-size: 14px;
}

.gallery {
  display: grid;
  gap: 12px;
}

.gallery-item {
  display: grid;
  grid-template-columns: 96px 1fr;
  gap: 12px;
  align-items: center;
}

.gallery-item img {
  width: 96px;
  height: 72px;
  object-fit: cover;
  border-radius: 12px;
}

.page-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
}

.page-link {
  border: none;
  padding: 12px 16px;
  border-radius: 12px;
  background: #eef2ff;
  text-align: left;
  font-weight: 600;
  color: #27325b;
  cursor: pointer;
}

.page-link.active {
  background: #dbe3ff;
}

.page-preview {
  background: #ffffff;
  padding: 16px;
  border-radius: 16px;
  box-shadow: inset 0 0 0 1px rgba(227, 232, 249, 0.8);
}

.page-content {
  color: #4c5565;
}

.split {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
}

.task-form,
.mail-form {
  display: grid;
  gap: 12px;
}

.task-item,
.mail-item {
  background: #f9fafc;
  border-radius: 12px;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.summary-card {
  margin-top: 16px;
  background: #f6f8ff;
  padding: 16px;
  border-radius: 12px;
}

.summary-controls {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.summary-select {
  min-width: 100px;
}

.auth-warning {
  padding: 16px;
  background: #fff5f5;
  border-radius: 12px;
  color: #a33b3b;
}

.mail-actions {
  display: flex;
  gap: 8px;
}

.admin-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.admin-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: 14px;
  background: #fff3e6;
  border: 1px solid rgba(255, 166, 0, 0.3);
}

.admin-banner h3 {
  margin: 0 0 4px;
  color: #9a4c07;
}

.admin-banner p {
  margin: 0;
  color: #8b5b2b;
}

.admin-hint {
  background: #f6f8ff;
  border-radius: 12px;
  padding: 12px 16px;
  color: #43547c;
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
}

.admin-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 16px;
  box-shadow: 0 12px 30px rgba(30, 41, 59, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.admin-card-header h4 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #1f2a44;
}

.admin-card-header span {
  color: #6a7387;
  font-size: 13px;
}

.admin-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 960px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .auth-card {
    width: 100%;
  }

  .top-bar {
    align-items: flex-start;
  }
}

@media (max-width: 600px) {
  .hero-actions {
    flex-direction: column;
  }

  .gallery-item {
    grid-template-columns: 1fr;
  }

  .section-header.with-back {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
