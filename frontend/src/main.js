import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import App from './App.vue';
import './style.css';
import './assets/styles/variables.css';

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('userRole');
      localStorage.removeItem('username');
      localStorage.removeItem('siteKey');
      delete axios.defaults.headers.common['X-Auth-Token'];
      ElMessage.error('Session expired. Please log in again.');
    }
    return Promise.reject(error);
  }
);

const app = createApp(App);
app.use(ElementPlus);
app.mount('#app');
