<template>
  <div class="page">
    <header v-if="currentView === 'home'" class="hero">
      <div class="hero-content">
        <p class="hero-tag">全能博客与导航平台</p>
        <h1>现代化主页入口，功能分区更清晰</h1>
        <p class="hero-subtitle">
          首页只展示功能入口，点击进入独立功能主页，专注高效浏览与操作体验。
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
        <div v-if="customPages.length" class="page-grid">
          <button v-for="page in customPages" :key="page.id" class="page-link">
            {{ page.title }}
          </button>
        </div>
        <el-empty v-else description="暂无自定义页面" />
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue';
import axios from 'axios';

const loading = ref(false);
const currentView = ref('home');
const navigationLinks = ref([]);
const searchEngines = ref([]);
const selectedEngine = ref(null);
const searchQuery = ref('');
const blogPosts = ref([]);
const albums = ref([]);
const customPages = ref([]);

const viewTitle = computed(() => {
  switch (currentView.value) {
    case 'navigation':
      return '导航中心';
    case 'blog':
      return '博客内容';
    case 'gallery':
      return '摄影展';
    case 'pages':
      return '自定义页面';
    default:
      return '功能入口';
  }
});

const viewDescription = computed(() => {
  switch (currentView.value) {
    case 'navigation':
      return '按分组查看常用站点，一键新标签打开。';
    case 'blog':
      return '浏览最新发布内容，专注阅读体验。';
    case 'gallery':
      return '查看精选摄影作品与专辑。';
    case 'pages':
      return '管理自定义页面入口。';
    default:
      return '选择进入独立功能主页。';
  }
});

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
    selectedEngine.value =
      searchEngines.value.find((engine) => engine.isDefault) || searchEngines.value[0] || null;
  } finally {
    loading.value = false;
  }
};

const switchView = (view) => {
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

onMounted(refreshAll);
</script>

<style scoped>
.page {
  min-height: 100vh;
  padding: 32px 24px 72px;
  background: linear-gradient(180deg, #f5f7ff 0%, #fdf8f2 100%);
  display: flex;
  flex-direction: column;
  gap: 40px;
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

.hero h1 {
  margin: 0 0 16px;
  font-size: clamp(28px, 3vw, 40px);
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

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
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
}

.split {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  gap: 24px;
}

@media (max-width: 768px) {
  .hero {
    grid-template-columns: 1fr;
  }

  .hero-actions {
    width: 100%;
    justify-content: flex-start;
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
