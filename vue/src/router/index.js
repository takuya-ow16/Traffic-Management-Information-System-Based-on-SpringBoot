import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '',
      redirect: '/login'
    },
    {
      path: '/user',
      component: () => import('../views/Manager.vue'),
      children:[
        {
          path: 'userDashboard',
          name: 'userDashboard',
          meta: {title: '首页'},
          component: () => import('../views/user/UserDashboard.vue'),
        },
        {
          path: 'UserProfile',
          name: 'UserUserProfile',
          meta: {title: '个人中心'},
          component: () => import('../views/user/UserProfile.vue'),
        },
        {
          path: 'myVehicles',
          name: 'myVehicles',
          meta: {title: '我的车辆'},
          component: () => import('../views/user/MyVehicles.vue'),
        },
        {
          path: 'MyViolation',
          name: 'MyViolation',
          meta: {title: '我的违章'},
          component: () => import('../views/user/MyViolation.vue'),
        },
        {
          path: 'road',
          name: 'road',
          meta: {title: '路况'},
          component: () => import('../views/user/Road.vue'),
        },
      ]
    },


    {
      path: '/police',
      component: () => import('../views/Manager.vue'),
      children:[
        {
          path: 'policeDashboard',
          name: 'policeDashboard',
          meta: {title: '首页'},
          component: () => import('../views/police/PoliceDashboard.vue'),
        },
        {
          path: 'UserProfile',
          name: 'PoliceUserProfile',
          meta: {title: '个人中心'},
          component: () => import('../views/user/UserProfile.vue'),
        },
        {
          path: 'Vehicles',
          name: 'Vehicles',
          meta: {title: '车辆管理'},
          component: () => import('../views/police/Vehicles.vue'),
        },
        {
          path: 'roadCondition',
          name: 'roadCondition',
          meta: {title: '路况管理'},
          component: () => import('../views/police/RoadCondition.vue'),
        },
        {
          path: 'violationManagement',
          name: 'violationManagement',
          meta: {title: '违章管理'},
          component: () => import('../views/police/violationManagement.vue'),
        },
      ]
    },
    {
      path: '/admin',
      component: () => import('../views/Manager.vue'),
      children:[
        {
          path: 'adminDashboard',
          name: 'adminDashboard',
          meta: {title: '首页'},
          component: () => import('../views/admin/AdminDashboard.vue'),
        },
        {
          path: 'UserProfile',
          name: 'AdminUserProfile',
          meta: {title: '个人中心'},
          component: () => import('../views/user/UserProfile.vue'),
        },
        {
          path: 'UserManagement',
          name: 'UserManagement',
          meta: {title: '用户管理'},
          component: () => import('../views/admin/UserManagement.vue'),
        },
        {
          path: 'PoliceManagement',
          name: 'PoliceManagement',
          meta: {title: '交警管理'},
          component: () => import('../views/admin/PoliceManagement.vue'),
        },
        {
          path: 'AdminManagement',
          name: 'AdminManagement',
          meta: {title: '管理员管理'},
          component: () => import('../views/admin/AdminManagement.vue'),
        },
      ]
    },
    {
      path: '/login',
      name: 'Login',
      meta: {title: '登录系统'},
      component: () => import('../views/user/Login.vue')
    },
    {
      path: '/register',
      name: 'Register',
      meta: {title: '欢迎注册'},
      component: () => import('../views/user/Register.vue')
    },
    {
      path: '/404',
      name: 'NOTFOUND',
      meta: {title: '404找不到页面'},
      component: () => import('../views/404.vue'),
    },
    {
      path: '/403',
      name: 'auth',
      meta: {title: '没有权限访问'},
      component: () => import('../views/Auth.vue'),
    },
    {
      path: '/:pathMatch(.*)', redirect: '/404'
    }
  ],
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '交通管理系统'
  
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const whiteList = ['/login', '/register', '/404', '/403']

  if (!user && !whiteList.includes(to.path)) {
    next('/login')
  } else if (user && (to.path === '/login' || to.path === '/register')) {
    // 已登录用户访问登录/注册页，重定向到对应首页
    if (user.role === 'ADM') next('/admin/adminDashboard')
    else if (user.role === 'POL') next('/police/policeDashboard')
    else next('/user/userDashboard')
  } else {
    // 权限校验
    if (to.path.startsWith('/admin') && user?.role !== 'ADM') {
      next('/403')
    } else if (to.path.startsWith('/police') && user?.role !== 'POL') {
      next('/403')
    } else if (to.path.startsWith('/user') && user?.role !== 'USER') {
      next('/403')
    } else {
      next()
    }
  }
})



export default router
