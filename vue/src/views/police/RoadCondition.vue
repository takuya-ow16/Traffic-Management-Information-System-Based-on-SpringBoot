<template>
  <div class="road-condition-container">
    <el-row :gutter="20" style="height: 100%">
      <!-- 1. 左侧道路选择栏 -->
      <el-col :span="4" style="height: 100%">
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
              {{ road.name }}
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 中间区域 -->
      <el-col :span="14" style="height: 100%; display: flex; flex-direction: column;">
        <!-- 2. 新增道路按钮区域 -->
        <div class="action-bar">
          <div class="btn-group">
            <el-button type="primary" :icon="Plus" @click="handleAddRoad">新增道路</el-button>
            <el-button type="warning" :icon="Edit" @click="handleEditRoad">修改监控</el-button>
            <el-popconfirm title="确定删除该道路及相关数据吗？" @confirm="handleDeleteRoad">
              <template #reference>
                <el-button type="danger" :icon="Delete">删除道路</el-button>
              </template>
            </el-popconfirm>
          </div>
          <span class="current-road-title" v-if="currentRoad">当前监控: {{ currentRoad.name }}</span>
        </div>

        <!-- 3. 视频展示控件 -->
        <el-card class="box-card video-card" body-style="padding: 0; height: 100%; background-color: #000;">
          <div v-if="currentVideoStreamUrl" :key="currentVideoStreamUrl" style="width: 100%; height: 100%; display: flex; align-items: center; justify-content: center;">
            <img 
              :src="currentVideoStreamUrl" 
              style="max-width: 100%; max-height: 100%; object-fit: contain;" 
              alt="Video Stream" 
              @error="handleVideoError"
            />
          </div>
          <div v-else class="video-placeholder">
            <el-icon :size="60" color="#fff"><VideoCamera /></el-icon>
            <p style="color: #fff; margin-top: 10px;">实时监控画面</p>
          </div>
        </el-card>

        <!-- 4. 近一天车流量统计图 -->
        <el-card class="box-card chart-card" body-style="padding: 10px; height: 100%;">
          <div ref="trafficChartRef" style="width: 100%; height: 100%;"></div>
        </el-card>
      </el-col>

      <!-- 右侧区域 -->
      <el-col :span="6" style="height: 100%; display: flex; flex-direction: column;">
        <!-- 5. 近15分钟车流量数据 -->
        <el-card class="box-card flow-card" body-style="padding: 20px; display: flex; align-items: center; justify-content: center; height: 100%;">
          <div class="stat-item">
            <div class="stat-title">近15分钟车流量</div>
            <div class="stat-value">{{ currentRoad?.flow15min || 0 }} <span class="unit">辆</span></div>
          </div>
        </el-card>

        <!-- 6. 近15分钟经过车辆车牌号 -->
        <el-card class="box-card plate-card" body-style="padding: 0; height: 100%; display: flex; flex-direction: column;">
          <template #header>
            <div class="card-header">
              <span>实时过车记录 (15min)</span>
            </div>
          </template>
          <el-table :data="plateList" style="width: 100%; flex: 1;" height="100%" stripe :show-header="true">
            <el-table-column prop="time" label="时间" width="90" />
            <el-table-column prop="plate" label="车牌号" align="center">
              <template #default="scope">
                <el-tag effect="dark" type="success">{{ scope.row.plate }}</el-tag>
              </template>
            </el-table-column>

          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/修改道路弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEditMode ? '修改道路监控' : '新增道路监控'" width="30%">
      <el-form :model="newRoadForm" label-width="100px">
        <el-form-item label="道路名称">
          <el-input v-model="newRoadForm.name" placeholder="请输入道路名称"></el-input>
        </el-form-item>
        <el-form-item label="上传视频/图片">
          <el-upload
            action="/api/files/upload"
            accept="video/*,image/*"
            :show-file-list="false"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
          >
            <el-button type="primary">点击上传</el-button>
          </el-upload>
          <div v-if="newRoadForm.fileUrl" style="margin-top: 5px; font-size: 12px; color: #67C23A;">
             <el-icon><CircleCheck /></el-icon> 上传成功
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveNewRoad">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { Plus, VideoCamera, CircleCheck, Edit, Delete } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import axios from 'axios'

// --- 数据定义 ---
const currentRoadIndex = ref(0)
const dialogVisible = ref(false)
const isEditMode = ref(false) // 区分新增还是编辑
const editingId = ref(null)   // 存储当前编辑的ID
const newRoadForm = reactive({ name: '', fileUrl: '' })
const trafficChartRef = ref(null)
let myChart = null
let pollInterval = null

// 道路列表数据
const roadList = ref([])

const currentRoad = computed(() => roadList.value[currentRoadIndex.value] || {})
const mediaRefreshToken = ref(Date.now())
const currentVideoStreamUrl = computed(() => {
  if (!currentRoad.value?.videoStreamUrl) return ''
  const separator = currentRoad.value.videoStreamUrl.includes('?') ? '&' : '?'
  return `${currentRoad.value.videoStreamUrl}${separator}_t=${mediaRefreshToken.value}`
})

// 车牌列表数据
const plateList = ref([])

// --- 方法 ---

// 加载道路列表
const loadRoads = () => {
  request.get('/road/selectAll').then(res => {
    if (res.code === '200') {
      roadList.value = res.data.map(item => ({
        ...item,
        fileUrl: item.linkaddress, // 后端字段是 linkaddress (注意大小写，根据 Entity 是 linkAddress 但 JSON 可能是 linkaddress)
        videoStreamUrl: item.linkaddress ? `/py-api/video_feed?url=${encodeURIComponent(item.linkaddress)}&roadId=${item.id}` : '',
        flow15min: 0,
        trend: 'up',
        trendValue: 0
      }))
      // 如果列表不为空且没有选中，默认选中第一个
      if (roadList.value.length > 0 && currentRoadIndex.value >= roadList.value.length) {
        currentRoadIndex.value = 0
      }
      mediaRefreshToken.value = Date.now()
    } else {
      ElMessage.error(res.msg || '获取道路列表失败')
    }
  })
}

const selectRoad = (index) => {
  currentRoadIndex.value = index
  mediaRefreshToken.value = Date.now()
  // 切换道路时刷新数据
  pollResults()
  // 清空当前车牌列表，重新开始轮询积累（或者如果有后端历史接口可以调用）
  plateList.value = [] 
  ElMessage.success(`切换至：${roadList.value[index].name}`)
}

const handleUploadSuccess = (res) => {
  if (res.code === '200') {
    newRoadForm.fileUrl = res.data
    ElMessage.success("上传成功")
  } else {
    ElMessage.error(res.msg)
  }
}

const handleUploadError = (error) => {
  const status = error?.status || error?.response?.status
  if (status === 413) {
    ElMessage.error('上传失败：文件过大，请同步放宽 Nginx 和 Spring Boot 的上传大小限制')
    return
  }
  ElMessage.error('上传失败，请检查文件格式、文件大小或服务器配置')
}

const handleAddRoad = () => {
  isEditMode.value = false
  editingId.value = null
  newRoadForm.name = ''
  newRoadForm.fileUrl = ''
  dialogVisible.value = true
}

const handleEditRoad = () => {
  if (!currentRoad.value || !currentRoad.value.id) {
    ElMessage.warning('请先选择一条道路')
    return
  }
  isEditMode.value = true
  editingId.value = currentRoad.value.id
  newRoadForm.name = currentRoad.value.name
  newRoadForm.fileUrl = currentRoad.value.fileUrl || ''
  dialogVisible.value = true
}

const handleDeleteRoad = () => {
  if (!currentRoad.value || !currentRoad.value.id) {
    ElMessage.warning('请先选择一条道路')
    return
  }
  request.delete(`/road/delete/${currentRoad.value.id}`).then(res => {
    if (res.code === '200') {
      ElMessage.success('删除成功')
      loadRoads() // 重新加载列表
    } else {
      ElMessage.error(res.msg || '删除失败')
    }
  })
}

const saveNewRoad = () => {
  if (!newRoadForm.name) {
    ElMessage.warning('请输入道路名称')
    return
  }
  if (!newRoadForm.fileUrl) {
    ElMessage.warning('请上传视频/文件')
    return
  }
  
  const roadData = {
    name: newRoadForm.name,
    linkaddress: newRoadForm.fileUrl
  }
  
  if (isEditMode.value) {
    roadData.id = editingId.value
    request.put('/road/update', roadData).then(res => {
      if (res.code === '200') {
        ElMessage.success('修改成功')
        dialogVisible.value = false
        loadRoads() // 重新加载列表
      } else {
        ElMessage.error(res.msg || '修改失败')
      }
    })
  } else {
    request.post('/road/add', roadData).then(res => {
      if (res.code === '200') {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        loadRoads() // 重新加载列表
      } else {
        ElMessage.error(res.msg || '添加失败')
      }
    })
  }
}

const handleVideoError = () => {
  ElMessage.warning('视频流连接中断或格式不支持，请检查后端服务')
  // 可以在这里将 currentRoad.videoStreamUrl 置空以显示占位符，或者保留以便重试
  // currentRoad.value.videoStreamUrl = '' 
}

// 轮询获取识别结果
const pollResults = () => {
  // 1. 获取最新车牌列表 (可选：如果您希望仅显示当前道路的数据，可以移除这部分通用的轮询)
  // axios.get('http://localhost:5000/get_latest_results') ...

  if (currentRoad.value && currentRoad.value.id) {
    // 2. 获取当前道路的 15分钟流量统计
    request.get(`/roadCondition/getFlow15Min/${currentRoad.value.id}`)
      .then(res => {
        if (res.code === '200') {
          currentRoad.value.flow15min = res.data
        }
      })
      .catch(err => console.error("Flow polling error:", err))

    // 3. 获取当前道路的 15分钟实时过车记录
    request.get(`/roadCondition/getRecords15Min/${currentRoad.value.id}`)
      .then(res => {
        if (res.code === '200') {
          // 直接覆盖，因为这是最新的、准确的、已去重的数据
          plateList.value = res.data.map(item => ({
            // 如果 time 格式是 'YYYY-MM-DD HH:mm:ss'，我们只取 'HH:mm:ss'
            time: item.time && item.time.includes(' ') ? item.time.split(' ')[1] : item.time,
            plate: item.plate
          }))
        }
      })
      .catch(err => console.error("Records polling error:", err))
  }
  
  // 4. 获取当前道路的 24小时流量趋势 (每2秒更新一次图表)
  updateChart()
}

// 初始化图表
const initChart = () => {
  if (trafficChartRef.value) {
    myChart = echarts.init(trafficChartRef.value)
    updateChart()
  }
}

const updateChart = () => {
  if (!myChart) return

  // 24小时数据
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  // 暂时用全0数据填充
  let data = Array(24).fill(0) 

  if (currentRoad.value && currentRoad.value.id) {
     request.get(`/roadCondition/getFlow24Hours/${currentRoad.value.id}`)
      .then(res => {
        if (res.code === '200') {
          // res.data 是 [{hour: '09', count: 5}, ...]
          res.data.forEach(item => {
            const hourIndex = parseInt(item.hour)
            if (hourIndex >= 0 && hourIndex < 24) {
              data[hourIndex] = item.count
            }
          })
          
          // 更新图表数据
          const option = {
            title: {
              text: '近24小时车流量趋势',
              left: 'center',
              textStyle: { fontSize: 16 }
            },
            tooltip: {
              trigger: 'axis'
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
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
                areaStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                    { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
                  ])
                },
                itemStyle: {
                  color: '#409EFF'
                }
              }
            ]
          }
          myChart.setOption(option)
        }
      })
      .catch(err => console.error("Chart data error:", err))
  } else {
      // 如果没有选中道路，显示空图表
      const option = {
        title: {
          text: '近24小时车流量趋势',
          left: 'center',
          textStyle: { fontSize: 16 }
        },
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
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
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
              ])
            },
            itemStyle: {
              color: '#409EFF'
            }
          }
        ]
      }
      myChart.setOption(option)
  }
}

// 监听窗口大小变化
const handleResize = () => {
  myChart && myChart.resize()
}

onMounted(() => {
  nextTick(() => {
    initChart()
    window.addEventListener('resize', handleResize)
    loadRoads() // 加载数据
    // 启动轮询
    pollInterval = setInterval(pollResults, 2000)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  myChart && myChart.dispose()
  if (pollInterval) clearInterval(pollInterval)
})

</script>

<style scoped>
.road-condition-container {
  height: calc(100vh - 100px); /* 减去顶部导航栏高度，根据实际布局调整 */
  padding: 10px;
  background-color: #f0f2f5;
  box-sizing: border-box;
}

.full-height {
  height: 100%;
}

.road-list {
  flex: 1;
  overflow-y: auto;
}

.road-item {
  padding: 12px 15px;
  cursor: pointer;
  border-bottom: 1px solid #EBEEF5;
  transition: background-color 0.3s;
  font-size: 14px;
}

.road-item:hover {
  background-color: #F5F7FA;
}

.road-item.active {
  background-color: #ECF5FF;
  color: #409EFF;
  font-weight: bold;
  border-right: 3px solid #409EFF;
}

.action-bar {
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.current-road-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.video-card {
  flex: 2; /* 视频区域占较大比例 */
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.video-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  background-color: #000;
  color: #909399;
}

.chart-card {
  flex: 1; /* 图表区域占较小比例 */
  min-height: 200px;
}

.flow-card {
  flex: 0 0 150px; /* 固定高度或比例 */
  margin-bottom: 10px;
  background: linear-gradient(135deg, #66b1ff 0%, #409EFF 100%);
  color: white;
}

.stat-item {
  text-align: center;
}

.stat-title {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin: 5px 0;
}

.unit {
  font-size: 14px;
  font-weight: normal;
}

.stat-trend {
  font-size: 12px;
}

.up {
  color: #ffcccc;
}

.down {
  color: #ccffcc;
}

.plate-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  font-weight: bold;
}
</style>
