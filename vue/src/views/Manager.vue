<!-- 基础布局组件 -->
<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="aside">
      <div class="logo" :class="{ 'logo-collapse': isCollapse }">
        <img src="@/assets/car.png" alt="logo" />
        <span v-show="!isCollapse">交通管理系统</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          class="menu"
          :router="true"
          :collapse="isCollapse"
          background-color="transparent"
          text-color="#e2e8f0"
          active-text-color="#60a5fa"
      >
        <!-- 管理员菜单 -->
        <template v-if="data.user.role === 'ADM'">
          <el-menu-item index="/admin/adminDashboard">
            <el-icon><Setting /></el-icon>
            <template #title>系统控制台</template>
          </el-menu-item>
          <el-sub-menu index="user-management">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/admin/UserManagement">
              <template #title>用户</template>
            </el-menu-item>
            <el-menu-item index="/admin/PoliceManagement">
              <template #title>交警</template>
            </el-menu-item>
            <el-menu-item index="/admin/AdminManagement">
              <template #title>管理员</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item index="/admin/UserProfile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </template>

        <!-- 交警菜单（与管理员类似但无用户管理） -->
        <template v-if="data.user.role === 'POL'">
          <el-menu-item index="/police/policeDashboard">
            <el-icon><Setting /></el-icon>
            <template #title>交警控制台</template>
          </el-menu-item>
          <el-menu-item index="/police/vehicles">
            <el-icon><Document /></el-icon>
            <template #title>车辆信息管理</template>
          </el-menu-item>
          <el-menu-item index="/police/violationManagement">
            <el-icon><Warning /></el-icon>
            <template #title>违章管理</template>
          </el-menu-item>
          <el-menu-item index="/police/roadCondition">
            <el-icon><View /></el-icon>
            <template #title>实时监控</template>
          </el-menu-item>
          <el-menu-item index="/police/UserProfile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </template>

        <!-- 普通用户菜单 -->
        <template v-if="data.user.role === 'USER'">
          <el-menu-item index="/user/userDashboard">
            <el-icon><Monitor /></el-icon>
            <template #title>首页</template>
          </el-menu-item>
          <el-menu-item index="/user/MyViolation">
            <el-icon><Warning /></el-icon>
            <template #title>我的违章</template>
          </el-menu-item>
          <el-menu-item index="/user/myVehicles">
            <el-icon><Document /></el-icon>
            <template #title>我的车辆</template>
          </el-menu-item>
          <el-menu-item index="/user/road">
            <el-icon><View /></el-icon>
            <template #title>路况</template>
          </el-menu-item>
          <el-menu-item index="/user/UserProfile">
            <el-icon><User /></el-icon>
            <template #title>个人中心</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主要内容区 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon
              class="collapse-btn"
              @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="data.user.avatar" />
              <span>{{ data.user.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view @updateUser="updateUser" />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import {ref, computed, reactive} from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Setting,
  User,
  View,
  Document,
  Warning,
  Monitor,
  Fold,
  Expand
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const data = reactive({
  isCollapse: false,
  user: JSON.parse(localStorage.getItem('user') || '{}'),
})

const activeMenu = computed(() => route.path)


const toggleCollapse = () => {
  data.isCollapse = !data.isCollapse
}

const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.removeItem('user')
    router.push('/login')
  } else if (command === 'profile') {
    if (data.user.role === 'ADM') {
      router.push('/admin/UserProfile')
    } else if (data.user.role === 'POL') {
      router.push('/police/UserProfile')
    } else {
      router.push('/user/UserProfile')
    }
  }
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem("user"))
}
</script>


<style scoped>
.layout-container {
  height: 100vh;
}

.aside {
  background: linear-gradient(180deg, #1e3a8a 0%, #1e40af 50%, #3b82f6 100%);
  transition: width 0.3s;
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  color: #fff;
  transition: all 0.3s;
  overflow: hidden;
  white-space: nowrap;
  background: rgba(255, 255, 255, 0.15);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  font-weight: 600;
  font-size: 16px;
}

.logo-collapse {
  padding: 0 16px;
}

.logo img {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  flex-shrink: 0;
}

.menu {
  border-right: none;
  background-color: transparent;
}

.menu:not(.el-menu--collapse) {
  width: 200px;
}

:deep(.el-menu-item) {
  &.is-active {
    background: rgba(251, 191, 36, 0.2);
    border-right: 3px solid #fbbf24;
    color: #fbbf24;
  }

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fbbf24;
  }
}

.header {
  background: linear-gradient(90deg, #f8fafc 0%, #f1f5f9 100%);
  border-bottom: 2px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 20px;
  transition: all 0.3s;
  color: #1e40af;
  padding: 8px;
  border-radius: 6px;

  &:hover {
    background: rgba(30, 64, 175, 0.1);
    transform: scale(1.1);
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.user-info span {
  margin-left: 8px;
}

.main {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  padding: 24px;
  min-height: calc(100vh - 64px);
}

/* 路由过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>