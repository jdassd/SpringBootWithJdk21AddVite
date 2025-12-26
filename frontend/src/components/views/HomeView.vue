<template>
  <section class="home-hero">
    <div class="hero-main">
      <span class="tagline">Public Site</span>
      <h1>{{ site?.displayName || 'Untitled Site' }}</h1>
      <p class="subtitle">{{ site?.description || 'A curated snapshot of links, stories, and visuals.' }}</p>
      <div class="actions">
        <el-button type="primary" size="large" @click="$emit('openRss')">Open RSS</el-button>
        <el-button size="large" @click="$emit('refresh')" :loading="loading">Refresh</el-button>
      </div>
    </div>

    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-value">{{ stats?.links ?? 0 }}</div>
        <div class="stat-label">Navigation Links</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats?.posts ?? 0 }}</div>
        <div class="stat-label">Public Posts</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">{{ stats?.albums ?? 0 }}</div>
        <div class="stat-label">Gallery Albums</div>
      </div>
    </div>
  </section>

  <section class="home-grid">
    <el-card class="home-card">
      <template #header>
        <div class="card-header">
          <span>Navigation</span>
          <el-tag size="small" effect="plain">{{ navigationLinks.length }}</el-tag>
        </div>
      </template>
      <div v-if="navigationLinks.length" class="nav-grid">
        <button
          v-for="link in navigationLinks.slice(0, 6)"
          :key="link.id"
          class="nav-link"
          @click="openLink(link.url)"
        >
          <span class="link-name">{{ link.name }}</span>
          <span class="link-group">{{ link.groupName || 'General' }}</span>
        </button>
      </div>
      <el-empty v-else description="No navigation links yet." />
    </el-card>

    <el-card class="home-card">
      <template #header>
        <div class="card-header">
          <span>Featured Posts</span>
        </div>
      </template>
      <div v-if="featuredPosts.length" class="post-list">
        <div v-for="post in featuredPosts" :key="post.id" class="post-item">
          <div>
            <div class="post-title">{{ post.title }}</div>
            <div class="post-meta">{{ formatDate(post.publishedAt || post.createdAt) }}</div>
            <div class="tag-row">
              <el-tag v-for="tag in (post.tags || []).slice(0, 3)" :key="tag" size="small" effect="plain">
                {{ tag }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
      <el-empty v-else description="No posts published yet." />
    </el-card>

    <el-card class="home-card">
      <template #header>
        <div class="card-header">
          <span>Announcements</span>
        </div>
      </template>
      <div v-if="announcements.length" class="announcement-list">
        <div v-for="post in announcements" :key="post.id" class="announcement-item">
          <div class="announcement-title">{{ post.title }}</div>
          <div class="announcement-meta">{{ formatDate(post.publishedAt || post.createdAt) }}</div>
        </div>
      </div>
      <el-empty v-else description="No announcements yet." />
    </el-card>

    <el-card class="home-card">
      <template #header>
        <div class="card-header">
          <span>Gallery</span>
        </div>
      </template>
      <div v-if="albums.length" class="gallery-preview">
        <div v-for="album in albums.slice(0, 4)" :key="album.id" class="album-preview">
          <img :src="album.coverUrl" alt="" />
          <div class="album-info">
            <div class="album-title">{{ album.title }}</div>
            <div class="album-desc">{{ album.description || 'Photo album' }}</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="No albums yet." />
    </el-card>
  </section>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  loading: Boolean,
  stats: Object,
  site: Object,
  navigationLinks: {
    type: Array,
    default: () => []
  },
  blogPosts: {
    type: Array,
    default: () => []
  },
  albums: {
    type: Array,
    default: () => []
  }
});

defineEmits(['openRss', 'refresh']);

const featuredPosts = computed(() => props.blogPosts.slice(0, 3));

const announcements = computed(() => {
  const tagged = props.blogPosts.filter((post) => {
    const tags = post.tags || [];
    return tags.includes('announcement') || tags.includes('notice');
  });
  return (tagged.length ? tagged : props.blogPosts).slice(0, 3);
});

const openLink = (url) => window.open(url, '_blank');

const formatDate = (value) => {
  if (!value) return '';
  return new Date(value).toLocaleDateString();
};
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
  font-size: 0.75rem;
}

h1 {
  font-size: 3rem;
  line-height: 1.1;
  margin: 16px 0 20px;
  color: var(--text-main);
  font-weight: 800;
}

.subtitle {
  font-size: 1.1rem;
  color: var(--text-secondary);
  margin-bottom: 24px;
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

.home-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 24px;
  margin-top: 12px;
}

.home-card {
  border-radius: var(--radius-lg);
  border: none;
  box-shadow: var(--shadow-sm);
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.nav-link {
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-color);
  text-align: left;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  gap: 6px;
  transition: var(--transition-fast);
}

.nav-link:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-sm);
}

.link-name {
  font-weight: 600;
  color: var(--text-main);
}

.link-group {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.post-item {
  border-bottom: 1px solid var(--border-color);
  padding-bottom: 12px;
}

.post-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.post-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.post-meta {
  color: var(--text-muted);
  font-size: 0.85rem;
}

.tag-row {
  margin-top: 8px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.announcement-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.announcement-item {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  background: var(--bg-color);
}

.announcement-title {
  font-weight: 600;
}

.announcement-meta {
  font-size: 0.8rem;
  color: var(--text-muted);
}

.gallery-preview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
  gap: 12px;
}

.album-preview {
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-color);
  border: 1px solid var(--border-color);
}

.album-preview img {
  width: 100%;
  height: 120px;
  object-fit: cover;
}

.album-info {
  padding: 8px;
}

.album-title {
  font-weight: 600;
  font-size: 0.9rem;
}

.album-desc {
  font-size: 0.8rem;
  color: var(--text-muted);
}
</style>
