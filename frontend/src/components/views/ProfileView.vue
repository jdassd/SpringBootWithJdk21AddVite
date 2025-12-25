<template>
  <div class="profile-view">
    <div class="profile-grid">
      <!-- Info Section -->
      <el-card class="profile-main modern-card">
        <template #header>
          <div class="card-header">
            <span class="header-title">基本信息</span>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存资料</el-button>
          </div>
        </template>

        <div class="profile-form-layout">
          <div class="avatar-edit-section">
            <el-avatar :size="100" :src="profile.avatarUrl">
              <el-icon :size="40"><UserFilled /></el-icon>
            </el-avatar>
            <div class="avatar-actions">
              <el-input 
                v-model="profile.avatarUrl" 
                placeholder="输入头像 URL" 
                size="small"
                style="margin-top: 12px"
              />
              <p class="hint-text">建议使用 200x200 以上的图片 URL</p>
            </div>
          </div>

          <el-form :model="profile" label-position="top" class="main-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="用户名">
                  <el-input v-model="profile.username" disabled />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="账户角色">
                  <el-tag :type="profile.role === 'ADMIN' ? 'danger' : 'success'">
                    {{ profile.role }}
                  </el-tag>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="电子邮箱">
              <el-input v-model="profile.email" placeholder="接收系统通知的邮箱" />
            </el-form-item>

            <el-form-item label="个人简介">
              <el-input 
                v-model="profile.bio" 
                type="textarea" 
                :rows="4" 
                placeholder="向他人介绍一下自己吧..."
              />
            </el-form-item>
          </el-form>
        </div>
      </el-card>

      <!-- Security Section -->
      <el-card class="profile-security modern-card">
        <template #header>
          <div class="card-header">
            <span class="header-title">安全中心</span>
          </div>
        </template>

        <el-form :model="pwForm" label-position="top">
          <el-form-item label="当前密码">
            <el-input v-model="pwForm.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwForm.newPassword" type="password" show-password placeholder="至少8位字符" />
          </el-form-item>
          <el-form-item label="确认新密码">
            <el-input v-model="pwForm.confirmPassword" type="password" show-password />
          </el-form-item>
          
          <el-button 
            type="warning" 
            style="width: 100%; margin-top: 10px"
            :loading="pwSaving"
            @click="changePassword"
          >
            修改登录密码
          </el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { UserFilled } from '@element-plus/icons-vue';

const profile = ref({
  username: '',
  email: '',
  role: '',
  avatarUrl: '',
  bio: ''
});

const pwForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const saving = ref(false);
const pwSaving = ref(false);

const fetchProfile = async () => {
  try {
    const res = await axios.get('/api/users/me');
    profile.value = res.data;
  } catch (e) {
    ElMessage.error('无法加载个人信息');
  }
};

const saveProfile = async () => {
  saving.value = true;
  try {
    await axios.put('/api/users/me', {
      email: profile.value.email,
      avatarUrl: profile.value.avatarUrl,
      bio: profile.value.bio
    });
    ElMessage.success('个人资料已更新');
  } catch (e) {
    ElMessage.error('保存失败');
  } finally {
    saving.value = false;
  }
};

const changePassword = async () => {
  if (pwForm.value.newPassword !== pwForm.value.confirmPassword) {
    return ElMessage.warning('两次输入的新密码不一致');
  }
  if (pwForm.value.newPassword.length < 8) {
    return ElMessage.warning('新密码长度需不少于 8 位');
  }

  pwSaving.value = true;
  try {
    await axios.post('/api/users/me/password', {
      oldPassword: pwForm.value.oldPassword,
      newPassword: pwForm.value.newPassword
    });
    ElMessage.success('密码修改成功');
    pwForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' };
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '密码修改失败，请检查原密码');
  } finally {
    pwSaving.value = false;
  }
};

onMounted(fetchProfile);
</script>

<style scoped>
.profile-view {
  min-height: 500px;
}

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 340px;
  gap: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-weight: 700;
  font-size: 1.1rem;
}

.profile-form-layout {
  display: flex;
  gap: 40px;
  padding: 10px 0;
}

.avatar-edit-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 200px;
}

.avatar-actions {
  width: 100%;
  text-align: center;
}

.hint-text {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.main-form {
  flex: 1;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
  .profile-form-layout {
    flex-direction: column;
    align-items: center;
  }
  .avatar-edit-section {
    width: 100%;
  }
}
</style>
