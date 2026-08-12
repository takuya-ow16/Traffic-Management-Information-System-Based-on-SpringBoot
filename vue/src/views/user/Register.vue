<!-- 注册页面 -->
<template>
  <div class="register-container">
    <div class="register-box">
      
      <!-- 右侧：注册表单区域 -->
      <div class="register-right">
        <div class="register-form-container">
          <h3 class="form-title">用户注册</h3>
          <el-form
            ref="registerFormRef"
            :model="data.user"
            :rules="data.rules"
            label-width="80px"
            size="large"
            class="register-form"
          >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="data.user.username"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="data.user.password"
            type="password"
            placeholder="请输入密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="data.user.confirmPassword"
            type="password"
            placeholder="请确认密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="真实姓名" prop="name">
          <el-input
            v-model="data.user.name"
            placeholder="请输入真实姓名"
          />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="data.user.idCard" placeholder="请输入身份证号" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="data.user.phone"
            placeholder="请输入手机号"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="data.user.email"
            placeholder="请输入邮箱"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="data.loading"
            class="register-button"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="register-options">
          已有账号？
          <router-link to="/login" class="login-link">
            立即登录
          </router-link>
        </div>
      </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import { User, Lock, Setting } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()

// 表单校验规则
const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== data.user.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

// 身份证校验（支持15位/18位，18位校验位支持X）
const validateIdNumber = (rule, value, callback) => {
  const v = (value || '').toString().trim()
  if (!v) {
    callback(new Error('请输入身份证号'))
    return
  }

  const isValid15 = /^[1-9]\d{7}(\d{2})(\d{2})(\d{2})\d{3}$/.test(v)
  const isValid18 = /^[1-9]\d{5}(19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[0-9Xx]$/.test(v)

  if (isValid15) {
    callback()
    return
  }

  if (!isValid18) {
    callback(new Error('请输入有效的身份证号'))
    return
  }

  // 校验18位校验位
  const factors = [7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2]
  const parity = ['1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2']
  const ai = v.substr(0, 17)
  let sum = 0
  for (let i = 0; i < 17; i++) {
    sum += parseInt(ai.charAt(i), 10) * factors[i]
  }
  const mod = sum % 11
  const code = parity[mod]
  const last = v.charAt(17).toUpperCase()
  if (code !== last) {
    callback(new Error('请输入有效的身份证号'))
    return
  }

  callback()
}

const data = reactive({
  user: {},
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入密码', trigger: 'blur' },
      { validator: validatePass2, trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '真实姓名长度应在2-20个字符之间', trigger: 'blur' }
    ],
    idCard: [
      { required: true, message: '请输入身份证号', trigger: 'blur' },
      { validator: validateIdNumber, trigger: 'blur' }
    ],
    phone: [
      { required: true, message: '请输入手机号', trigger: 'blur' },
      { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
    ],
    email: [
      { required: true, message: '请输入邮箱', trigger: 'blur' },
      { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
    ]
  },
  loading: false
})

const registerFormRef = ref()

// 处理注册
const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate((valid, fields) => {
    if (valid) {
      data.loading = true
      request.post('/register', data.user).then(res => {
        if (res.code === '200') {
          ElMessage.success('注册成功，请登录')
          router.push('/login')
        } else {
          ElMessage.error(res.msg || '注册失败')
        }
      }).catch(err => {
        ElMessage.error('注册请求失败，请稍后重试')
        console.error(err)
      }).finally(() => {
        data.loading = false
      })
    } else {
      console.log('提交错误!', fields)
    }
  })
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('@/assets/imgs/login-bg.jpg') no-repeat center center;
  background-size: cover;
  background-attachment: fixed;
  position: relative;
  padding: 20px 0;
}

/* 移除整体背景模糊遮罩，保持背景图清晰 */

.register-box {
  width: 1000px;
  height: 800px;
  max-width: 95%;
  padding: 0;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  overflow: hidden;
  margin: 20px;
  display: flex;
}

.system-intro {
  position: relative;
  z-index: 1;
  width: 100%;
}

.logo-section {
  margin-bottom: 30px;
}

.logo-img {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  padding: 6px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(3px);
}

.system-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 1;
}

.system-subtitle {
  font-size: 16px;
  margin: 0 0 40px 0;
  opacity: 0.9;
  font-weight: 300;
  letter-spacing: 2px;
  position: relative;
  z-index: 1;
}

.system-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-top: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  opacity: 0.9;
  
  .el-icon {
    font-size: 20px;
    color: #60a5fa;
  }
}

.register-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.register-form-container {
  width: 100%;
  max-width: 400px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  text-align: center;
}

.form-subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 30px 0;
  text-align: center;
}

.register-form {
  width: 100%;
}

.register-button {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(30, 64, 175, 0.3);
  margin-top: 5px;
}

.register-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(30, 64, 175, 0.4);
}

.register-button:active {
  transform: translateY(0);
}

.register-options {
  margin-top: 15px;
  text-align: center;
  padding-bottom: 5px;
}

.login-link {
  color: #1e40af;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 6px 12px;
  border-radius: 8px;
  display: inline-block;
}

.login-link:hover {
  color: #1e3a8a;
  background: rgba(30, 64, 175, 0.1);
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .register-box {
    width: 95%;
    height: auto;
    flex-direction: column;
  }
  
  .register-right {
    padding: 30px 20px;
  }
  
  .system-title {
    font-size: 24px;
  }
  
  .system-subtitle {
    font-size: 14px;
  }
  
  .form-title {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .register-box {
    width: 98%;
    margin: 10px;
  }
  
  
  .register-right {
    padding: 25px 15px;
  }
  
  .logo-img {
    width: 60px;
    height: 60px;
  }
  
  .system-title {
    font-size: 20px;
  }
  
  .form-title {
    font-size: 20px;
  }
}
</style> 