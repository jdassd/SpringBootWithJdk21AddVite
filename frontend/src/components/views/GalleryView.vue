<template>
  <div class="gallery-view">
    <transition name="fade-transform" mode="out-in">
      <!-- Photos View (Inside an Album) -->
      <div v-if="selectedAlbum" :key="'photos-' + selectedAlbum.id" class="album-details">
        <el-page-header @back="selectedAlbum = null">
          <template #content>
            <span class="detail-title">{{ selectedAlbum.title }}</span>
          </template>
        </el-page-header>
        
        <el-divider />

        <div v-if="albumLoading" class="loading-state">
          <el-skeleton :rows="5" animated />
        </div>
        
        <div v-else-if="photos.length" class="photo-grid">
          <div v-for="(photo, index) in photos" :key="photo.id" class="photo-item">
            <el-image 
              :src="photo.imageUrl" 
              :preview-src-list="allPhotoUrls"
              :initial-index="index"
              fit="cover"
              class="gallery-image"
              loading="lazy"
              preview-teleported
            >
              <template #placeholder>
                <div class="image-slot">加载中...</div>
              </template>
            </el-image>
            <div class="photo-caption">{{ photo.title }}</div>
          </div>
        </div>
        <el-empty v-else description="该相册暂无照片" />
      </div>

      <!-- Albums View -->
      <div v-else :key="'albums'" class="gallery-grid">
        <el-card 
          v-for="album in albums" 
          :key="album.id" 
          class="album-card modern-card clickable-card" 
          :body-style="{ padding: '0px' }"
          @click="handleAlbumClick(album)"
        >
          <div class="album-cover-wrapper">
            <img :src="album.coverUrl" class="album-image" />
            <div class="album-count">
              <el-icon><Picture /></el-icon> 展开查看
            </div>
          </div>
          <div class="album-info">
            <h4>{{ album.title }}</h4>
            <p>{{ album.description || '暂无描述' }}</p>
          </div>
        </el-card>
        <el-empty v-if="!albums.length" description="暂无摄影集" />
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { Picture } from '@element-plus/icons-vue';
import axios from 'axios';

const props = defineProps({
  albums: {
    type: Array,
    default: () => []
  }
});

const selectedAlbum = ref(null);
const photos = ref([]);
const albumLoading = ref(false);

const allPhotoUrls = computed(() => photos.value.map(p => p.imageUrl));

const handleAlbumClick = async (album) => {
  selectedAlbum.value = album;
  albumLoading.value = true;
  try {
    const res = await axios.get(`/api/gallery/public/albums/${album.id}/photos`);
    photos.value = res.data;
  } catch (e) {
    console.error('Failed to fetch photos', e);
  } finally {
    albumLoading.value = false;
  }
};
</script>

<style scoped>
.gallery-view {
  min-height: 400px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.clickable-card {
  cursor: pointer;
  overflow: hidden;
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.clickable-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}

.album-cover-wrapper {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.album-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.clickable-card:hover .album-image {
  transform: scale(1.05);
}

.album-count {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 8px;
  background: rgba(0,0,0,0.6);
  color: white;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 4px;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.clickable-card:hover .album-count {
  opacity: 1;
}

.album-info {
  padding: 16px;
}

.album-info h4 {
  margin: 0 0 8px;
  font-size: 1.1rem;
}

.album-info p {
  font-size: 0.9rem;
  color: var(--text-secondary);
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* Photo Grid */
.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.photo-item {
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--card-bg);
  box-shadow: var(--shadow-sm);
}

.gallery-image {
  width: 100%;
  height: 180px;
  display: block;
}

.photo-caption {
  padding: 8px;
  font-size: 0.85rem;
  text-align: center;
  background: #f9f9f9;
  color: var(--text-secondary);
}

.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background: #eee;
  color: #999;
}

.detail-title {
  font-weight: 700;
  font-size: 1.25rem;
}
</style>
