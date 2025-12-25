<template>
  <div class="home-hero">
    <div class="hero-main">
      <span class="tagline">全能博客与导航平台</span>
      <h1>现代化主页入口，<br/>功能分区更清晰</h1>
      <p class="subtitle">集成博客、摄影展、任务提醒与导航入口，为您提供一站式的数字工作空间。</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="$emit('openRss')">订阅 RSS</el-button>
        <el-button size="large" @click="$emit('refresh')" :loading="loading">刷新内容</el-button>
      </div>
    </div>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats.links }}</div>
        <div class="stat-label">导航链接</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.posts }}</div>
        <div class="stat-label">公开文章</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats.albums }}</div>
        <div class="stat-label">摄影专辑</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  loading: Boolean,
  stats: Object
});
defineEmits(['openRss', 'refresh']);
</script>

<style scoped>
.home-hero {
  padding: 40px 0;
  display: flex;
  flex-direction: column;
  gap: 40px;
}

.hero-main {
  max-width: 800px;
}

.tagline {
  color: var(--primary-color);
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  font-size: 0.875rem;
}

h1 {
  font-size: 3.5rem;
  line-height: 1.1;
  margin: 16px 0 24px;
  color: var(--text-main);
  font-weight: 800;
}

.subtitle {
  font-size: 1.25rem;
  color: var(--text-secondary);
  margin-bottom: 32px;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 24px;
}

.stat-card {
  background: var(--card-bg);
  padding: 24px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  text-align: center;
  transition: var(--transition-normal);
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.stat-value {
  font-size: 2rem;
  font-weight: 800;
  color: var(--primary-color);
  margin-bottom: 4px;
}

.stat-label {
  color: var(--text-secondary);
  font-weight: 500;
}
</style>
