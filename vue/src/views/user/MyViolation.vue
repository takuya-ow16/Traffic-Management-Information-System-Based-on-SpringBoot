<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input style="width: 240px;margin-right: 5px" v-model="data.name" placeholder="请输入车牌号查询" prefix-icon="Search"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="primary" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" @click="handleAppeal">我要申诉</el-button>
      <el-button type="danger" @click="handleAdd">我要举报</el-button>
    </div>


    <div class="card" style="margin-bottom: 5px">
      <el-table :data="data.tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"/>
        <el-table-column label="图片">
          <template #default="scope">
            <img v-if="scope.row.image" :src="scope.row.image" style="display: block; width: 40px; height: 40px; border-radius: 50%">
          </template>
        </el-table-column>
        <el-table-column label="车牌号" prop="plate" />
        <el-table-column label="车主姓名" prop="ownerName" />
        <el-table-column label="车主身份证" prop="ownerId" />
        <el-table-column label="违章时间" prop="time" />
        <el-table-column label="违章原因" prop="violationName" show-overflow-tooltip/>
        <el-table-column label="违章金额" prop="violationMoney" />
        <el-table-column label="违章分数" prop="violationPoints" />
        <el-table-column label="违章状态" prop="status" >
          <template #default="scope">
            <el-tag :type="scope.row.status === 'UND' ? 'warning' : scope.row.status === 'UNT' ? 'danger' : scope.row.status === 'HPD' ? 'success' : scope.row.status === 'AED' ? 'info' : scope.row.status === 'PAS' ? 'success' : 'primary'" size="small">
              {{ (scope.row.status === 'UNT' && scope.row.infoid === user.idCard) ? '举报成功' : scope.row.status === 'UND' ? '举报中' : scope.row.status === 'UNT' ? '未处理' : scope.row.status === 'HPD' ? '已支付' : scope.row.status === 'AED' ? '已申诉' : scope.row.status === 'PAS' ? '申诉成功' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button @click="handleView(scope.row)" type="success" :icon="Document" circle></el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 10px">
        <el-pagination
            @current-change="load"
            @size-change="load"
            v-model:current-page="data.pageNum"
            v-model:page-size="data.pageSize"
            :page-sizes="[5, 10, 15, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="data.total"
        />
      </div>
    </div>

<!--  -->

    <el-dialog title="违章详情" v-model="data.viewVisible" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车牌号">{{ data.viewData.plate }}</el-descriptions-item>
        <el-descriptions-item label="车主">{{ data.viewData.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="车型">{{ data.viewData.carModel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="违章时间">{{ data.viewData.time }}</el-descriptions-item>
        <el-descriptions-item label="违章地点">{{ data.viewData.address }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ data.viewData.violationName }}</el-descriptions-item>
        <el-descriptions-item label="驳回理由" :span="2" v-if="data.viewData.appealReply">
           <div v-html="data.viewData.appealReply" style="max-height: 200px; overflow-y: auto; color: red"></div>
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="data.viewData.status === 'UND' ? 'warning' : data.viewData.status === 'UNT' ? 'danger' : data.viewData.status === 'HPD' ? 'success' : data.viewData.status === 'AED' ? 'info' : data.viewData.status === 'PAS' ? 'success' : 'primary'" size="small">
            {{ (data.viewData.status === 'UNT' && data.viewData.infoid === user.idCard) ? '举报成功' : data.viewData.status === 'UND' ? '举报中' : data.viewData.status === 'UNT' ? '未处理' : data.viewData.status === 'HPD' ? '已支付' : data.viewData.status === 'AED' ? '已申诉' : data.viewData.status === 'PAS' ? '申诉成功' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.viewVisible = false">关闭</el-button>
          <el-button type="primary" @click="handlePay" v-if="data.viewData.status === 'UNT'">去支付</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog  :title="data.from.id ? '编辑违章' : '新增违章'" v-model="data.formVisible" width="700" destroy-on-close @close="resetFormState">
      <el-form ref="formRef" :rules="data.rules" :model="data.from" style="padding-top: 20px">
<!--        上传用户头像-->
        <el-form-item label="违章照片" label-width="80px" prop="plate">
          <div >
            <el-upload
                action="/api/files/upload"
                list-type="picture"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
            >
              <el-button type="primary">上传照片</el-button>
            </el-upload>
          </div>
        </el-form-item>


        <el-form-item label="车辆" label-width="80px" prop="plate">
          <el-select v-model="data.from.plate" style="width: 100%" placeholder="请选择车辆" @change="handleCarChange">
            <el-option v-for="item in data.cars.filter(c => c.ownerId !== user.idCard)" :label="item.plate" :value="item.plate" :key="item.plate"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="车辆信息">
          <div class="vehicle-info">
              <div><strong>VIN:</strong> {{ data.selectCar.vin || '-' }}</div>
              <div><strong>车型:</strong> {{ data.selectCar.model || '-' }}</div>
              <div><strong>品牌:</strong> {{ data.selectCar.brand || '-' }}</div>
              <div><strong>颜色:</strong> {{ data.selectCar.color || '-' }}</div>
              <div><strong>车主身份证:</strong> {{ data.selectCar.ownerId || '-' }}</div>
              <div><strong>发动机号:</strong> {{ data.selectCar.engine || '-' }}</div>
            </div>
        </el-form-item>

        <el-form-item label="违章时间" prop="time">
          <el-date-picker 
            v-model="data.from.time" 
            type="date" 
            placeholder="选择违章时间" 
            style="width: 100%" 
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="违章地点" label-width="80px">
          <el-input v-model="data.from.address" autocomplete="off" placeholder="请输入违章地点"/>
        </el-form-item>

        <el-form-item label="违章类型" label-width="80px" prop="plate">
          <el-select v-model="data.from.violationid" style="width: 100%" placeholder="请选择违章类型" @change="handleViolationIdChange">
            <el-option v-for="item in data.violationIds" :label="item.name" :value="item.id" :key="item.id"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="违章信息">
          <div class="vehicle-info">
            <div><strong>金额:</strong> {{ data.selectViolationId.money || '-' }}</div>
            <div><strong>分数:</strong> {{ data.selectViolationId.points || '-' }}</div>
          </div>
        </el-form-item>

      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.formVisible = false">取消</el-button>
          <el-button type="primary" @click="save"> 保存 </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="申诉" v-model="data.appealVisible" width="700px" destroy-on-close>
      <el-form :model="data.appealForm" label-width="80px">
        <el-form-item label="选择违章">
          <el-select v-model="data.appealForm.violationId" placeholder="请选择未处理的违章" style="width: 100%" @change="handleAppealViolationChange">
            <el-option v-for="item in data.untreatedViolations" :key="item.id" :label="item.violationName + ' (' + item.time + ')'" :value="item.id">
              <span style="float: left">{{ item.violationName }}</span>
              <span style="float: right; color: #8492a6; font-size: 13px">{{ item.time }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="违章信息" v-if="data.selectAppealViolation.id">
          <div class="vehicle-info" style="width: 100%">
            <div><strong>车牌:</strong> {{ data.selectAppealViolation.plate }}</div>
            <div><strong>时间:</strong> {{ data.selectAppealViolation.time }}</div>
            <div><strong>地点:</strong> {{ data.selectAppealViolation.address }}</div>
            <div><strong>金额:</strong> {{ data.selectAppealViolation.violationMoney }}</div>
            <div><strong>分数:</strong> {{ data.selectAppealViolation.violationPoints }}</div>
            <div><strong>状态:</strong> 
              <el-tag type="danger" size="small">未处理</el-tag>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="申诉理由">
          <div style="border: 1px solid #ccc; width: 100%">
            <Toolbar
                style="border-bottom: 1px solid #ccc"
                :editor="editorRef"
                :defaultConfig="toolbarConfig"
                :mode="'default'"
            />
            <Editor
                style="height: 300px; overflow-y: hidden;"
                v-model="data.appealForm.content"
                :defaultConfig="editorConfig"
                :mode="'default'"
                @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.appealVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAppeal">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref, shallowRef, onBeforeUnmount} from "vue";
import {Delete, Edit, Search, Document} from "@element-plus/icons-vue"
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";
import { useRouter } from 'vue-router'
import '@wangeditor/editor/dist/css/style.css' // 引入 css
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || '{}')

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

const toolbarConfig = {}
const editorConfig = { placeholder: '请输入内容...' }

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor) => {
    editorRef.value = editor // 记录 editor 实例，重要！
}

const handleAppeal = () => {
  data.appealVisible = true
  data.appealForm = {}
  loadUntreatedViolations()
}

const loadUntreatedViolations = () => {
  request.get('/user/selectViolationPage', {
    params: {
      pageNum: 1,
      pageSize: 100, // 获取足够多的未处理违章
      status: 'UNT'
    }
  }).then(res => {
    if (res.code === '200') {
      // 仅显示属于自己的违章，过滤掉自己举报的
      data.untreatedViolations = res.data.list.filter(item => item.ownerId === user.idCard)
    }
  })
}

const handleAppealViolationChange = (id) => {
  const violation = data.untreatedViolations.find(item => item.id === id)
  if (violation) {
    data.selectAppealViolation = violation
  }
}

const saveAppeal = () => {
  if (!data.appealForm.violationId) {
    ElMessage.warning("请选择违章单")
    return
  }
  if (!data.appealForm.content) {
    ElMessage.warning("请填写申诉理由")
    return
  }
  
  // 构建请求参数
  const params = {
    violationId: data.appealForm.violationId,
    content: data.appealForm.content,
    violationStatus: 'AED' // 明确设置状态为申诉中
  }

  request.post('/appealed/add', params).then(res => {
    if (res.code === '200') {
      ElMessage.success("申诉提交成功")
      data.appealVisible = false
      load() // 刷新列表
    } else {
      ElMessage.error(res.msg)
    }
  })
}


const handleCarChange = (plate) => {
  const car = data.cars.find(item => item.plate === plate)
  if (car) {
    data.selectCar = car
    data.from.plate = car.plate
    data.from.ownerId = car.ownerId
  }
}

const handleViolationIdChange = (id) => {
  const violationId = data.violationIds.find(item => item.id === id)
  if (violationId) {
    data.selectViolationId = violationId
    data.from.violationid = violationId.id
  }
}

const data = reactive({
  formVisible: false,
  name:null,
  total:0,
  tableData:[],
  pageNum:1,
  pageSize: 10,
  from:{},
  ids:[],
  rules: {
  },
  departmentList: [],
  status:[
    {value:"NOR", label:"正常"},
    {value:"OUT", label:"注销"}
  ],
  cars: [], //获取所有车的数据
  selectCar:{},
  violationIds:[],//获取所有的违章代码及其数据
  selectViolationId: {},
  viewVisible: false,
  viewData: {},
  appealVisible: false,
  appealForm: {},
  untreatedViolations: [],
  selectAppealViolation: {}
})

const formRef = ref()

const load = () => {
  request.get('/user/selectViolationPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        plate: data.name
      }
    }).then(res => {
      data.tableData = res.data.list
      data.total = res.data.total
  })
}

load()

const loadCar = () => {
  request.get('/car/selectAll').then(res => {
    // 对返回的数据进行去重，以 plate 为准，防止重复 key 导致显示异常
    const uniqueCars = []
    const map = new Map()
    if (res.data && res.data.length > 0) {
      res.data.forEach(item => {
        if (item.plate && !map.has(item.plate)) {
          map.set(item.plate, true)
          uniqueCars.push(item)
        }
      })
    }
    data.cars = uniqueCars
  })
}

loadCar() //先获取所有数据

const loadViolationId = () => {   //获取所有的违章信息
  request.get("/violationId/selectAll").then(res => {
    data.violationIds = res.data
  })
}

loadViolationId()

request.get('/police/selectAll').then(res => {
  data.departmentList = res.data
})

const handleAvatarSuccess = (res) => {
  if (res.code === '200') {
    data.from.image = res.data
  } else {
    ElMessage.error(res.msg)
  }
}

const reset = () => {
  data.name = null
  load()
}

const handleAdd = () => {
  data.formVisible = true
  data.from = {
    plate: ''
  }
}

const save = () => {
  formRef.value.validate((valid) => {
    if (valid) {
      add()
      data.formVisible = false
    }
  })
}

const add = () => {
  request.post('/violation/userAdd', data.from).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleView = (row) => {
  data.viewData = JSON.parse(JSON.stringify(row))
  const car = data.cars.find(item => item.plate === row.plate)
  if (car) {
    data.viewData.carModel = car.model
  }
  
  // 尝试获取申诉/驳回信息
  request.get('/appealed/selectById/' + row.id).then(res => {
    if (res.code === '200' && res.data) {
      if (res.data.reply) {
        data.viewData.appealReply = res.data.reply
      }
    }
  })
  
  data.viewVisible = true
}

const handlePay = () => {
  ElMessageBox.confirm("确认支付吗？","支付确认",{ type: 'warning'}).then(()=>{
    request.put('/violation/userHandleViolation', data.viewData).then(res => {
      if (res.code === '200') {
        ElMessage.success("支付成功")
        data.viewVisible = false
        
        // 获取最新用户信息检查分数
        const user = JSON.parse(localStorage.getItem('user') || '{}')
        if (user.id) {
          request.get('/user/selectById/' + user.id).then(userRes => {
            if (userRes.code === '200') {
              const latestUser = userRes.data
              // 更新本地存储（保留token）
              const token = user.token
              const updatedUser = { ...latestUser, token: token }
              localStorage.setItem('user', JSON.stringify(updatedUser))

              if (latestUser.points <= 0) {
                ElMessage.warning("您的驾驶分已扣光，账号已被冻结")
                localStorage.removeItem('user')
                router.push('/login')
              } else {
                load()
              }
            } else {
              load()
            }
          })
        } else {
          load()
        }
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}

const resetFormState = () => {
  data.selectCar = {}
  data.selectViolationId = {}
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(row => row.id)
}

</script>



<style scoped>
  .violation-page { padding: 20px }
  .vehicle-info { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px }
  .previews { margin-top: 8px; display:flex; gap:8px; flex-wrap:wrap }
  .preview-img { width: 140px; height: 90px; object-fit:cover; border:1px solid #eaeaea; border-radius:4px }
</style>
