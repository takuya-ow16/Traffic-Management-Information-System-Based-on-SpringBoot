<template>
  <div class="auth-container">
    <el-result
        icon="warning"
        title="无权访问"
        sub-title="抱歉，您没有权限访问该页面"
    >
      <template #extra>
        <el-button type="primary" @click="$router.push('/login')">返回登录</el-button>
        <el-button @click="$router.back()">返回上一页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
</script>

<style scoped>
.auth-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}
</style>
