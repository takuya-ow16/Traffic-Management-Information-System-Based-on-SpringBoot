<!-- 登录页面 -->
<template>
  <div class="login-container">
    <div class="login-box">
      <!-- 右侧：登录表单区域 -->
      <div class="login-right">
        <div class="login-form-container">
          <h3 class="form-subtitle">基于Spring Boot的交通管理信息系统</h3>
          <h3 class="form-title">用户登录</h3>
          <el-form
              ref="loginFormRef"
              :model="data.user"
              :rules="data.rules"
              label-width="0"
              size="large"
              class="login-form"
          >
            <el-form-item prop="username">
              <el-input
                  v-model="data.user.username"
                  placeholder="用户名"
                  :prefix-icon="User"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                  v-model="data.user.password"
                  type="password"
                  placeholder="密码"
                  :prefix-icon="Lock"
                  show-password
                  @keyup.enter="handleLogin"
              />
            </el-form-item>

            <!-- 验证码 -->
            <el-form-item prop="code">
            <div style="display: flex; width: 100%">
              <el-input size="large" placeholder="请输入验证码" v-model="data.user.code" style="width: 200px"></el-input>
              <div style="flex: 1; height: 40px">
                <vild-code @input="getCode"/>
              </div>
            </div>
          </el-form-item>

            <el-form-item style="align-items: center;width: 100%">
              <el-radio-group v-model="data.user.role" fill="#409eff" >
                <el-radio-button label="管理员" value="ADM" />
                <el-radio-button label="交警" value="POL" />
                <el-radio-button label="普通用户" value="USER" />
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button
                  type="primary"
                  :loading="data.loading"
                  class="login-button"
                  @click="handleLogin"
              >
                登录
              </el-button>
            </el-form-item>
            <div class="login-options">
              <router-link to="/register" class="register-link">
                还没有账号？立即注册
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
import {ElMessage} from 'element-plus'
import { Monitor, Warning, DataAnalysis, User, Lock } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/utils/request'
import VildCode from "@/components/VildCode.vue";


const router = useRouter()
const route = useRoute()

const loginFormRef = ref()
const generatedCode = ref('')

const getCode = (code) => {
  generatedCode.value = code.toLowerCase()
}

const ValidateCode = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请输入验证码'))
  } else if (value.toLowerCase() !== generatedCode.value) {
    callback(new Error('验证码错误'))
  } else {
    callback()
  }
}

const data = reactive({
  user: {
    role: "ADM",
    username: '',
    password: '',
    code: ''
  },
  rules: {
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
    ],
    code: [
      { validator: ValidateCode, trigger: 'blur' }
    ]
  },
  loading: false
})

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  loginFormRef.value.validate(async (valid) => {
    if (valid) {
      data.loading = true
      try {
        const res = await request.post('/login', data.user)
        if (res.code === '200') {
          ElMessage.success('登录成功')
          localStorage.setItem('user', JSON.stringify(res.data))
          
          // 根据角色跳转
          if (res.data.role === 'ADM') {
            router.push('/admin/adminDashboard')
          } else if (res.data.role === 'POL') {
            router.push('/police/policeDashboard')
          } else {
            router.push('/user/userDashboard')
          }
        } else {
          ElMessage.error(res.msg || '登录失败')
        }
      } catch (error) {
        console.error('登录错误', error)
        ElMessage.error('登录系统异常')
      } finally {
        data.loading = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: url('@/assets/imgs/login-bg.jpg') no-repeat center center;
  background-size: cover;
  background-attachment: fixed;
  position: relative;
}

/* 移除整体背景模糊遮罩，保持背景图清晰 */

.login-box {
  width: 900px;
  height: 600px;
  padding: 0;
  background: rgba(255, 255, 255, 0.97);
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(5px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  position: relative;
  z-index: 1;
  overflow: hidden;
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
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 3px solid rgba(255, 255, 255, 0.3);
  padding: 8px;
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

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-form-container {
  width: 100%;
  max-width: 350px;
}

.form-title {
  font-size: 28px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
  text-align: center;
}

.form-subtitle {
  font-size: 30px;
  color: #050606;
  margin: 0 0 40px 0;
  text-align: center;
}

.login-form {
  width: 100%;
}

.login-button {
  width: 100%;
  height: 50px;
  border-radius: 12px;
  background: linear-gradient(135deg, #1e40af 0%, #1e3a8a 100%);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(30, 64, 175, 0.3);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(30, 64, 175, 0.4);
}

.login-button:active {
  transform: translateY(0);
}

.login-options {
  margin-top: 25px;
  text-align: center;
  padding-bottom: 10px;
}

.register-link {
  color: #1e40af;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
  padding: 8px 16px;
  border-radius: 8px;
  display: inline-block;
}

.register-link:hover {
  color: #1e3a8a;
  background: rgba(30, 64, 175, 0.1);
  transform: translateY(-1px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-box {
    width: 95%;
    height: auto;
    flex-direction: column;
  }

  .login-right {
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
  .login-box {
    width: 98%;
    margin: 10px;
  }



  .login-right {
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