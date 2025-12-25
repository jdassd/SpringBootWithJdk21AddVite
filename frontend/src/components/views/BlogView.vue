<template>
  <div class="blog-view">
    <transition name="fade-transform" mode="out-in">
      <!-- Detail View -->
      <div v-if="selectedPost" :key="'detail-' + selectedPost.id" class="post-detail">
        <el-page-header @back="selectedPost = null">
          <template #content>
            <span class="detail-title">{{ selectedPost.title }}</span>
          </template>
          <template #extra>
            <div class="post-meta">
              <el-tag size="small" type="info">{{ formatDate(selectedPost.createdAt) }}</el-tag>
            </div>
          </template>
        </el-page-header>
        
        <el-divider />
        
        <div class="post-content markdown-body" v-html="renderMarkdown(selectedPost.content)"></div>
      </div>

      <!-- List View -->
      <div v-else :key="'list'" class="post-grid">
        <el-card 
          v-for="post in posts" 
          :key="post.id" 
          class="post-card modern-card clickable-card" 
          shadow="hover"
          @click="selectedPost = post"
        >
          <div class="post-card-header">
            <h3>{{ post.title }}</h3>
            <span class="post-date">{{ formatDate(post.createdAt) }}</span>
          </div>
          <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
          <div class="post-footer">
            <el-button link type="primary">阅读全文 <el-icon><ArrowRight /></el-icon></el-button>
          </div>
        </el-card>
        <el-empty v-if="!posts.length" description="暂无发表的文章" />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { ArrowRight } from '@element-plus/icons-vue';
import MarkdownIt from 'markdown-it';

const props = defineProps({
  posts: {
    type: Array,
    default: () => []
  }
});

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
});

const selectedPost = ref(null);

const renderMarkdown = (content) => {
  return md.render(content || '');
};

const getExcerpt = (content) => {
  if (!content) return '';
  const plainText = content.replace(/[#*`>]/g, '');
  return plainText.length > 120 ? plainText.slice(0, 120) + '...' : plainText;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};
</script>

<style scoped>
.blog-view {
  min-height: 400px;
}

.post-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.clickable-card {
  cursor: pointer;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.clickable-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.post-card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.post-card-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-main);
}

.post-date {
  font-size: 0.85rem;
  color: var(--text-secondary);
  white-space: nowrap;
}

.post-excerpt {
  color: var(--text-secondary);
  font-size: 0.95rem;
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: flex-end;
}

/* Detail Styling */
.post-detail {
  background: var(--card-bg);
  padding: 40px;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
}

.detail-title {
  font-weight: 700;
  font-size: 1.5rem;
}

.post-content {
  line-height: 1.8;
  font-size: 1.1rem;
  color: var(--text-main);
}

:deep(.markdown-body) img {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: 20px 0;
}

:deep(.markdown-body) pre {
  background: var(--bg-color);
  padding: 16px;
  border-radius: var(--radius-md);
  overflow-x: auto;
}

:deep(.markdown-body) code {
  font-family: 'Fira Code', monospace;
  background: rgba(0,0,0,0.05);
  padding: 2px 4px;
  border-radius: 4px;
}
</style>
