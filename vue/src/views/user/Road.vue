<template>
  <div class="road-container">
    <el-row :gutter="20" style="height: 100%">
      <!-- 1. 左侧道路选择栏 -->
      <el-col :span="5" style="height: 100%">
        <el-card class="box-card full-height" body-style="padding: 0; height: 100%; display: flex; flex-direction: column;">
          <template #header>
            <div class="card-header">
              <span>道路列表</span>
            </div>
          </template>
          <div class="road-list">
            <div 
              v-for="(road, index) in roadList" 
              :key="index" 
              class="road-item" 
              :class="{ active: currentRoadIndex === index }"
              @click="selectRoad(index)"
            >
              <div class="road-name">{{ road.name }}</div>
              <el-tag size="small" :type="getCongestionType(road.status)">{{ road.status }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧区域 -->
      <el-col :span="19" style="height: 100%; display: flex; flex-direction: column;">
        
        <!-- 2. 近一天车流量统计图 -->
        <el-card class="box-card section-card chart-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <span>近24小时车流量趋势</span>
            </div>
          </template>
          <div ref="trafficChartRef" style="width: 100%; height: 100%;"></div>
        </el-card>

        <!-- 3. 近15分钟车流量数据 -->
        <el-card class="box-card section-card flow-card" shadow="hover">
          <div class="flow-content">
            <div class="flow-icon">
              <el-icon :size="40" color="#409EFF"><Timer /></el-icon>
            </div>
            <div class="flow-info">
              <div class="label">近15分钟车流量</div>
              <div class="value">{{ currentRoad?.flow15min || 0 }} <span class="unit">辆</span></div>
            </div>
          </div>
        </el-card>

        <!-- 4. 道路拥堵判断控件 -->
        <el-card class="box-card section-card status-card" shadow="hover" :body-style="{ padding: '0px', height: '100%' }">
          <div class="status-container" :class="getCongestionClass(currentRoad?.status)">
            <div class="status-left">
              <div class="status-title">当前道路状态</div>
              <div class="status-badge">{{ currentRoad?.status }}</div>
            </div>
            <div class="status-right">
              <div class="congestion-meter">
                <span class="meter-label">拥堵指数: {{ currentRoad?.congestionIndex }}</span>
                <el-progress 
                  :text-inside="true" 
                  :stroke-width="24" 
                  :percentage="currentRoad?.congestionIndex * 10" 
                  :status="getProgressStatus(currentRoad?.status)"
                  striped
                  striped-flow
                />
              </div>
              <div class="status-desc">
                {{ getStatusDescription(currentRoad?.status) }}
              </div>
            </div>
          </div>
        </el-card>

      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { Timer } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// --- 数据定义 ---
const currentRoadIndex = ref(0)
const trafficChartRef = ref(null)
let myChart = null

// 道路数据
const roadList = ref([])

const currentRoad = computed(() => roadList.value[currentRoadIndex.value] || {})

// --- 方法 ---

// 加载道路列表
const loadRoads = () => {
  request.get('/road/selectAll').then(res => {
    if (res.code === '200') {
      roadList.value = res.data.map(item => ({
        ...item,
        flow15min: 0,
        // 以下字段目前后端暂无，设为默认值或根据流量计算
        status: '未知', 
        congestionIndex: 0, 
        avgSpeed: 0
      }))
      // 如果有道路数据，加载第一条道路的详细数据
      if (roadList.value.length > 0) {
        selectRoad(0)
      }
    } else {
      ElMessage.error(res.msg || '获取道路列表失败')
    }
  })
}

const selectRoad = (index) => {
  currentRoadIndex.value = index
  // 获取当前道路的详细数据
  if (currentRoad.value && currentRoad.value.id) {
    // 1. 获取15分钟流量
    request.get(`/roadCondition/getFlow15Min/${currentRoad.value.id}`).then(res => {
      if (res.code === '200') {
        currentRoad.value.flow15min = res.data
        // 简单的拥堵状态判断逻辑 (仅供演示，实际应由后端返回或复杂算法计算)
        if (res.data > 200) {
          currentRoad.value.status = '拥堵'
          currentRoad.value.congestionIndex = (res.data / 30).toFixed(1)
          currentRoad.value.avgSpeed = 15
        } else if (res.data > 100) {
          currentRoad.value.status = '缓行'
          currentRoad.value.congestionIndex = (res.data / 40).toFixed(1)
          currentRoad.value.avgSpeed = 30
        } else {
          currentRoad.value.status = '畅通'
          currentRoad.value.congestionIndex = (res.data / 100).toFixed(1)
          currentRoad.value.avgSpeed = 60
        }
      }
    })
    
    // 2. 更新24小时趋势图
    updateChart()
  }
}

const getCongestionType = (status) => {
  switch (status) {
    case '畅通': return 'success'
    case '缓行': return 'warning'
    case '拥堵': return 'danger'
    default: return 'info'
  }
}

const getProgressStatus = (status) => {
  switch (status) {
    case '畅通': return 'success'
    case '缓行': return 'warning'
    case '拥堵': return 'exception'
    default: return ''
  }
}

const getCongestionClass = (status) => {
  switch (status) {
    case '畅通': return 'bg-success'
    case '缓行': return 'bg-warning'
    case '拥堵': return 'bg-danger'
    default: return ''
  }
}

const getStatusDescription = (status) => {
  switch (status) {
    case '畅通': return '道路通畅，车辆行驶速度较快，建议保持车距。'
    case '缓行': return '车流量较大，行车缓慢，请耐心驾驶，注意安全。'
    case '拥堵': return '当前路段严重拥堵，建议绕行其他路线。'
    default: return '暂无数据'
  }
}

// 初始化图表
const initChart = () => {
  if (trafficChartRef.value) {
    myChart = echarts.init(trafficChartRef.value)
    // 移除初始化的 updateChart，因为数据还没加载
  }
}

const updateChart = () => {
  if (!myChart) return
  if (!currentRoad.value || !currentRoad.value.id) return

  // 24小时数据
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  // 暂时用全0数据填充
  let data = Array(24).fill(0) 

  request.get(`/roadCondition/getFlow24Hours/${currentRoad.value.id}`).then(res => {
    if (res.code === '200') {
      res.data.forEach(item => {
        const hourIndex = parseInt(item.hour)
        if (hourIndex >= 0 && hourIndex < 24) {
          data[hourIndex] = item.count
        }
      })
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '10%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: hours
        },
        yAxis: {
          type: 'value',
          name: '流量(辆)'
        },
        series: [
          {
            name: '车流量',
            type: 'line',
            smooth: true,
            data: data,
            itemStyle: {
              color: '#409EFF'
            },
            areaStyle: {
               color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ])
            }
          }
        ]
      }
      myChart.setOption(option)
    }
  })
}

// 监听窗口大小变化
const handleResize = () => {
  myChart && myChart.resize()
}

onMounted(() => {
  nextTick(() => {
    initChart()
    window.addEventListener('resize', handleResize)
    loadRoads() // 页面加载时获取道路列表
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart && myChart.dispose()
})

</script>

<style scoped>
.road-container {
  height: calc(100vh - 100px);
  padding: 10px;
  background-color: #f0f2f5;
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

.section-card {
  margin-bottom: 15px;
}
.section-card:last-child {
  margin-bottom: 0;
}

/* 布局比例分配 */
.chart-card {
  flex: 3; /* 占比最大 */
  display: flex;
  flex-direction: column;
}
.chart-card :deep(.el-card__body) {
  flex: 1;
  padding: 10px;
}

.flow-card {
  flex: 1;
}

.status-card {
  flex: 1.5;
  overflow: hidden;
}

/* 道路列表样式 */
.road-list {
  flex: 1;
  overflow-y: auto;
}

.road-item {
  padding: 15px;
  cursor: pointer;
  border-bottom: 1px solid #EBEEF5;
  transition: all 0.3s;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.road-item:hover {
  background-color: #F5F7FA;
}

.road-item.active {
  background-color: #ECF5FF;
  border-left: 4px solid #409EFF;
}

.road-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

/* 15分钟流量卡片 */
.flow-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 100%;
}

.flow-icon {
  background-color: #ecf5ff;
  padding: 15px;
  border-radius: 50%;
}

.flow-info {
  text-align: center;
}
.flow-info .label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}
.flow-info .value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
}
.flow-info .unit {
  font-size: 14px;
  font-weight: normal;
}


/* 拥堵状态卡片 */
.status-container {
  display: flex;
  height: 100%;
  align-items: center;
}

.status-left {
  flex: 0 0 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  border-right: 1px solid rgba(255,255,255,0.2);
  background-color: rgba(0,0,0,0.02);
}

.status-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.status-badge {
  font-size: 24px;
  font-weight: bold;
  padding: 5px 20px;
  border-radius: 20px;
  color: white;
  background-color: #909399;
}

.status-right {
  flex: 1;
  padding: 0 30px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.congestion-meter {
  margin-bottom: 15px;
}
.meter-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
  font-weight: bold;
}

.status-desc {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

/* 状态颜色主题 */
.bg-success .status-badge { background-color: #67C23A; box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4); }
.bg-warning .status-badge { background-color: #E6A23C; box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4); }
.bg-danger .status-badge { background-color: #F56C6C; box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4); }

.bg-success { background: linear-gradient(to right, #f0f9eb, #ffffff); }
.bg-warning { background: linear-gradient(to right, #fdf6ec, #ffffff); }
.bg-danger { background: linear-gradient(to right, #fef0f0, #ffffff); }

</style>
