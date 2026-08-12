<!-- 个人中心页面 -->
<template>
  <div class="profile">
    <el-row :gutter="20">
      <!-- 个人信息卡片 -->
      <el-col :span="8">
        <el-card class="profile-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">用户信息</span>
              <el-button
                type="primary"
                link
                @click="handleEdit"
              >
                <el-icon><Edit /></el-icon>
                编辑资料
              </el-button>
            </div>
          </template>
          <div class="profile-info">
            <div class="avatar-wrapper">
              <el-avatar
                :size="100"
                :src="data.user.avatar"
              />
              <el-upload
                class="avatar-uploader"
                action="/api/files/upload"
                :auto-upload="true"
                name="file"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
              >
                <el-button
                  type="primary"
                  action
                  link
                  class="change-avatar"
                >
                  <el-icon><Camera /></el-icon>
                  更换头像
                </el-button>
              </el-upload>
            </div>
            <div class="info-list">
              <div class="info-item">
                <span class="label">用户名：</span>
                <span class="value">{{ data.user.username }}</span>
              </div>
              <div class="info-item">
                <span class="label">真实姓名：</span>
                <span class="value">{{ data.user.name }}</span>
              </div>
              <div class="info-item">
                <span class="label">身份证号：</span>
                <span class="value">{{ data.user.idCard }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号：</span>
                <span class="value">{{ data.user.phone }}</span>
              </div>
              <div class="info-item">
                <span class="label">邮箱：</span>
                <span class="value">{{ data.user.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">角色：</span>
                <el-tag :type="data.user.role === 'ADM' ? 'success' : data.user.role === 'POL' ? 'primary' : 'info' " size="small">
                  {{ data.user.role === 'ADM' ? '管理员' : data.user.role === 'POL' ? '交警' : '普通用户' }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="label">状态：</span>
                <el-tag :type="data.user.status === 'NOR' ? 'success' : data.user.status === 'SQU' ? 'warning' : 'danger'" size="small">
                  {{ data.user.status === 'NOR' ? '正常' : data.user.status === 'SQU' ? '违章' : '封禁' }}
                </el-tag>
              </div>
              <div class="info-item">
                <span class="label">驾驶分：</span>
                <el-tag :type="data.user.points >=  6 ? 'success' : data.user.points >=  3 ? 'warning' : 'danger'" size="small">
                  {{ data.user.points }}
                </el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 修改密码卡片 -->
      <el-col :span="8">
        <el-card class="password-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="header-title">修改密码</span>
              <el-icon><Lock /></el-icon>
            </div>
          </template>
          <el-form
            ref="passwordFormRef"
            :model="data.passwordForm"
            :rules="data.passwordRules"
            label-width="100px"
            status-icon
          >
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="data.passwordForm.oldPassword"
                type="password"
                show-password
                placeholder="请输入原密码"
              />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="data.passwordForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码"
              />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="data.passwordForm.confirmPassword"
                type="password"
                show-password
                placeholder="请确认新密码"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="data.passwordLoading"
                @click="handleChangePassword"
              >
                <el-icon><Check /></el-icon>
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑个人信息对话框 -->
    <el-dialog
      v-model="data.editDialogVisible"
      title="编辑个人信息"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="editFormRef"
        :model="data.editForm"
        :rules="data.editRules"
        label-width="100px"
        status-icon
      >
        <el-form-item label="用户名" prop="username">
          <el-input
              v-model="data.editForm.username"
              placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input
            v-model="data.editForm.name"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard" v-if="data.user.role !== 'ADM'">
          <el-input
              v-model="data.editForm.idCard"
              placeholder="请输入身份证号"
              disabled
          />
        </el-form-item>
        <el-form-item label="手机号" prop="phone" v-if="data.user.role !== 'ADM'">
          <el-input
            v-model="data.editForm.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email" v-if="data.user.role !== 'ADM'">
          <el-input
            v-model="data.editForm.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.editDialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="data.editLoading"
            @click="updataUser"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, computed, reactive} from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Camera, Lock, Check } from '@element-plus/icons-vue'
import request from "@/utils/request.js";
import router from '@/router';

// 修改密码表单校验规则
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== data.passwordForm.newPassword) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 处理编辑
const handleEdit = () => {
  // 优先使用从后端获取的最新数据 data.from，如果未获取到则兜底使用 localStorage 中的 data.user
  data.editForm = { ...(data.from || data.user) }
  data.editDialogVisible = true
}



const data = reactive({
  user: JSON.parse(localStorage.getItem('user') || '{}'),
  editDialogVisible: false,
  passwordLoading: false,
  editLoading: false,

  passwordForm: {
    oldPassword:'',
    newPassword:'',
    confirmPassword:''
  },

  passwordRules: {
    oldPassword: [
      { required: true, message: '请输入原密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
    ],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      { validator: validatePass2, trigger: 'blur' }
    ]
  },

  editRules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '真实姓名长度应在2-20个字符之间', trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  },
  editForm: {

  },

  from: {},
})

// 获取最新用户信息
const loadUserInfo = () => {
  if (data.user.role === 'ADM') {
    request.get('/admin/selectById/' + data.user.id).then(res => {
      // 保留原有 token
      const token = data.user.token
      data.user = { ...data.user, ...res.data }
      data.user.token = token // 确保 token 不丢失
      data.from = res.data
      data.passwordForm = res.data
      localStorage.setItem('user', JSON.stringify(data.user))
    })
  } else if (data.user.role === 'POL') {
    request.get('/police/selectById/' + data.user.id).then(res => {
      // 保留原有 token
      const token = data.user.token
      data.user = { ...data.user, ...res.data }
      data.user.token = token // 确保 token 不丢失
      data.from = res.data
      data.passwordForm = res.data
      localStorage.setItem('user', JSON.stringify(data.user))
    })
  } else if (data.user.role === 'USER') {
    request.get('/user/selectById/' + data.user.id).then(res => {
      // 保留原有 token
      const token = data.user.token
      data.user = { ...data.user, ...res.data }
      data.user.token = token // 确保 token 不丢失
      data.from = res.data
      data.passwordForm = res.data
      localStorage.setItem('user', JSON.stringify(data.user))
    })
  }
}

loadUserInfo() // 页面加载时调用

const emit = defineEmits(['updateUser'])

// if (data.user.role === 'ADM') {
//   request.get('/admin/selectById/' + data.user.id).then(res => {
//     data.from = res.data
//     data.passwordForm = res.data
//   })
// } else if (data.user.role === 'POL') {
//   request.get('/police/selectById/' + data.user.id).then(res => {
//     data.from = res.data
//     data.passwordForm = res.data
//   })
// } else if (data.user.role === 'USER') {
//   request.get('/user/selectById/' + data.user.id).then(res => {
//     data.from = res.data
//     data.passwordForm = res.data
//   })
// }

const editFormRef = ref()

const updataUser = () => {
  editFormRef.value.validate((valid) => {
    if (valid) {
      if (data.user.role === 'ADM') {
        request.put('/admin/update', data.editForm).then(res => {
          if (res.code === '200') {
            ElMessage.success("更新成功")
            // 更新本地存储的用户信息，保留 token
            const updatedUser = { ...data.user, ...data.editForm };
            // 确保 token 不会被 data.editForm 中的 null 值覆盖
            if (data.user.token) {
              updatedUser.token = data.user.token;
            }
            localStorage.setItem('user', JSON.stringify(updatedUser))
            // 更新当前页面显示的数据
            data.user = updatedUser;
            // 更新最新数据源，以便下次编辑时使用最新数据
            data.from = { ...data.editForm };
            data.editDialogVisible = false; // 关闭弹窗
            emit('updateUser')
          } else {
            ElMessage.error(res.msg)
          }
        })
      } else if (data.user.role === 'POL') {
        request.put('/police/update', data.editForm).then(res => {
          if (res.code === '200') {
            ElMessage.success("更新成功")
            // 更新本地存储的用户信息，保留 token
            const updatedUser = { ...data.user, ...data.editForm };
            // 确保 token 不会被 data.editForm 中的 null 值覆盖
            if (data.user.token) {
              updatedUser.token = data.user.token;
            }
            localStorage.setItem('user', JSON.stringify(updatedUser))
            // 更新当前页面显示的数据
            data.user = updatedUser;
            // 更新最新数据源，以便下次编辑时使用最新数据
            data.from = { ...data.editForm };
            data.editDialogVisible = false; // 关闭弹窗
            emit('updateUser')
          } else {
            ElMessage.error(res.msg)
          }
        })
      } else if (data.user.role === 'USER') {
        request.put('/user/update', data.editForm).then(res => {
          if (res.code === '200') {
            ElMessage.success("更新成功")
            // 更新本地存储的用户信息，保留 token
            const updatedUser = { ...data.user, ...data.editForm };
            // 确保 token 不会被 data.editForm 中的 null 值覆盖
            if (data.user.token) {
              updatedUser.token = data.user.token;
            }
            localStorage.setItem('user', JSON.stringify(updatedUser))
            // 更新当前页面显示的数据
            data.user = updatedUser;
            // 更新最新数据源，以便下次编辑时使用最新数据
            data.from = { ...data.editForm };
            data.editDialogVisible = false; // 关闭弹窗
            emit('updateUser')
          } else {
            ElMessage.error(res.msg)
          }
        })
      } else {
        ElMessage.error("数据错误")
      }
    }
  })
}

const handleAvatarSuccess = (res) => {
  data.from.avatar = res.data
  data.user.avatar = res.data

  let url = ''
  if (data.user.role === 'ADM') url = '/admin/update'
  else if (data.user.role === 'POL') url = '/police/update'
  else if (data.user.role === 'USER') url = '/user/update'

  if (url) {
    request.put(url, data.user).then(res => {
      if (res.code === '200') {
        ElMessage.success('头像更换成功')
        localStorage.setItem('user', JSON.stringify(data.user))
        emit('updateUser')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
}

const passwordFormRef = ref()

// 处理修改密码
const handleChangePassword = () => {
  passwordFormRef.value.validate((valid) => {
    if (valid) {
        request.put('/updatePassword', data.passwordForm).then(res =>{
          if (res.code === '200') {
            ElMessage.success('修改成功')
            localStorage.removeItem('user')
            setTimeout(() => {
              location.href = "/login"
            }, 500)
          } else {
            ElMessage.error(res.msg)
          }
        })
    }
  })
}


</script>

<style scoped>
.profile {
  padding: 20px;
  min-height: calc(100vh - 140px);
  background-color: #f5f7fa;
}

.profile-card,
.password-card {
  height: 100%;
  transition: all 0.3s;
}

.profile-card:hover,
.password-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
  display: flex;
  align-items: center;
  gap: 8px;
}

.profile-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: relative;
  margin-bottom: 30px;
  text-align: center;
}

.change-avatar {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-list {
  width: 100%;
}

.info-item {
  display: flex;
  margin-bottom: 20px;
  padding: 0 20px;
  line-height: 24px;
}

.info-item .label {
  width: 80px;
  color: #606266;
  font-weight: 500;
}

.info-item .value {
  color: #303133;
  flex: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

</style> 