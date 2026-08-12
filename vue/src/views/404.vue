<template>
  <div class="not-found-container">
    <el-result
        icon="error"
        title="404"
        sub-title="抱歉，您访问的页面不存在"
    >
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
        <el-button @click="$router.back()">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) {
    if (user.role === 'ADM') {
      router.push('/admin/adminDashboard')
    } else if (user.role === 'POL') {
      router.push('/police/policeDashboard')
    } else {
      router.push('/user/userDashboard')
    }
  } else {
    router.push('/login')
  }
}
</script>

<style scoped>
.not-found-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
</style>
