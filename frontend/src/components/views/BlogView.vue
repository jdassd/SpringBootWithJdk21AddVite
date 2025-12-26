<template>
  <div class="blog-view">
    <div v-if="!selectedPost" class="blog-toolbar">
      <el-select v-model="selectedTag" placeholder="Filter by tag" clearable>
        <el-option v-for="tag in tags" :key="tag" :label="tag" :value="tag" />
      </el-select>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        start-placeholder="Start date"
        end-placeholder="End date"
        unlink-panels
        value-format="YYYY-MM-DD"
      />
      <el-button @click="resetFilters">Reset</el-button>
    </div>

    <transition name="fade-transform" mode="out-in">
      <div v-if="selectedPost" :key="'detail-' + selectedPost.id" class="post-detail">
        <el-page-header @back="closeDetail">
          <template #content>
            <span class="detail-title">{{ selectedPost.title }}</span>
          </template>
          <template #extra>
            <div class="post-meta">
              <el-tag size="small" type="info">{{ formatDate(selectedPost.publishedAt || selectedPost.createdAt) }}</el-tag>
            </div>
          </template>
        </el-page-header>

        <div class="tag-row" v-if="selectedPost.tags?.length">
          <el-tag v-for="tag in selectedPost.tags" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
        </div>

        <el-divider />

        <div class="post-content markdown-body" v-html="renderMarkdown(selectedPost.content)"></div>

        <el-divider />

        <section class="comment-section">
          <h3>Comments</h3>
          <div class="comment-form">
            <el-input v-model="commentForm.author" placeholder="Name" />
            <el-input v-model="commentForm.content" type="textarea" rows="3" placeholder="Write a comment" />
            <el-button type="primary" @click="submitComment" :disabled="commentSubmitting">Submit</el-button>
          </div>
          <div v-if="comments.length" class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <strong>{{ comment.author }}</strong>
                <span>{{ formatDate(comment.createdAt) }}</span>
              </div>
              <div class="comment-body" v-html="renderMarkdown(comment.content)"></div>
            </div>
          </div>
          <el-empty v-else description="No comments yet." />
        </section>

        <section v-if="relatedPosts.length" class="related-section">
          <h3>Related Posts</h3>
          <div class="related-list">
            <div
              v-for="post in relatedPosts"
              :key="post.id"
              class="related-item"
              @click="openDetail(post)"
            >
              <div class="related-title">{{ post.title }}</div>
              <div class="related-date">{{ formatDate(post.publishedAt || post.createdAt) }}</div>
            </div>
          </div>
        </section>
      </div>

      <div v-else :key="'list'" class="post-grid">
        <el-card
          v-for="post in filteredPosts"
          :key="post.id"
          class="post-card modern-card clickable-card"
          shadow="hover"
          @click="openDetail(post)"
        >
          <div class="post-card-header">
            <h3>{{ post.title }}</h3>
            <span class="post-date">{{ formatDate(post.publishedAt || post.createdAt) }}</span>
          </div>
          <p class="post-excerpt">{{ getExcerpt(post.content) }}</p>
          <div class="tag-row" v-if="post.tags?.length">
            <el-tag v-for="tag in post.tags.slice(0, 3)" :key="tag" size="small" effect="plain">{{ tag }}</el-tag>
          </div>
          <div class="post-footer">
            <el-button link type="primary">Read more <el-icon><ArrowRight /></el-icon></el-button>
          </div>
        </el-card>
        <el-empty v-if="!filteredPosts.length" description="No posts found." />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue';
import { ArrowRight } from '@element-plus/icons-vue';
import MarkdownIt from 'markdown-it';
import axios from 'axios';

const props = defineProps({
  posts: {
    type: Array,
    default: () => []
  },
  siteKey: {
    type: String,
    required: true
  }
});

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
});

const selectedPost = ref(null);
const selectedTag = ref('');
const dateRange = ref([]);
const comments = ref([]);
const commentForm = ref({ author: '', content: '' });
const commentSubmitting = ref(false);

const tags = computed(() => {
  const allTags = props.posts.flatMap((post) => post.tags || []);
  return [...new Set(allTags)];
});

const filteredPosts = computed(() => {
  let list = [...props.posts];
  if (selectedTag.value) {
    list = list.filter((post) => (post.tags || []).includes(selectedTag.value));
  }
  if (dateRange.value?.length === 2) {
    const [start, end] = dateRange.value.map((val) => new Date(val).getTime());
    list = list.filter((post) => {
      const stamp = new Date(post.publishedAt || post.createdAt).getTime();
      return stamp >= start && stamp <= end;
    });
  }
  return list;
});

const relatedPosts = computed(() => {
  if (!selectedPost.value?.tags?.length) return [];
  return props.posts.filter((post) => {
    if (post.id === selectedPost.value.id) return false;
    return (post.tags || []).some((tag) => selectedPost.value.tags.includes(tag));
  }).slice(0, 3);
});

const renderMarkdown = (content) => md.render(content || '');

const getExcerpt = (content) => {
  if (!content) return '';
  const plainText = content.replace(/[#*`>]/g, '');
  return plainText.length > 120 ? `${plainText.slice(0, 120)}...` : plainText;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
};

const openDetail = async (post) => {
  selectedPost.value = post;
  await loadComments(post.id);
};

const closeDetail = () => {
  selectedPost.value = null;
  comments.value = [];
  commentForm.value = { author: '', content: '' };
};

const resetFilters = () => {
  selectedTag.value = '';
  dateRange.value = [];
};

const loadComments = async (postId) => {
  try {
    const res = await axios.get(`/api/public/${props.siteKey}/blog/posts/${postId}/comments`);
    comments.value = res.data || [];
  } catch (e) {
    comments.value = [];
  }
};

const submitComment = async () => {
  if (!selectedPost.value) return;
  if (!commentForm.value.author || !commentForm.value.content) return;
  commentSubmitting.value = true;
  try {
    await axios.post(`/api/public/${props.siteKey}/blog/posts/${selectedPost.value.id}/comments`, {
      author: commentForm.value.author,
      content: commentForm.value.content
    });
    commentForm.value = { author: '', content: '' };
    await loadComments(selectedPost.value.id);
  } finally {
    commentSubmitting.value = false;
  }
};

watch(() => props.posts, () => {
  if (selectedPost.value) {
    const updated = props.posts.find((post) => post.id === selectedPost.value.id);
    if (updated) selectedPost.value = updated;
  }
});
</script>

<style scoped>
.blog-view {
  min-height: 400px;
}

.blog-toolbar {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
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
  line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.post-footer {
  display: flex;
  justify-content: flex-end;
}

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

.tag-row {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.comment-section {
  margin-top: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  padding: 12px;
  border-radius: var(--radius-md);
  background: var(--bg-color);
  border: 1px solid var(--border-color);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.comment-body {
  font-size: 0.95rem;
}

.related-section {
  margin-top: 24px;
}

.related-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
}

.related-item {
  padding: 12px;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition-fast);
}

.related-item:hover {
  border-color: var(--primary-color);
}

.related-title {
  font-weight: 600;
}

.related-date {
  font-size: 0.8rem;
  color: var(--text-muted);
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
