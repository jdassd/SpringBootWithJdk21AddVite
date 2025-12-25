import { ElMessage } from 'element-plus';

export const notifySuccess = (message) => {
  ElMessage({
    type: 'success',
    message,
    showClose: true,
    duration: 3000,
  });
};

export const notifyError = (message) => {
  ElMessage({
    type: 'error',
    message,
    showClose: true,
    duration: 3500,
  });
};

export const notifyWarning = (message) => {
  ElMessage({
    type: 'warning',
    message,
    showClose: true,
    duration: 3200,
  });
};
