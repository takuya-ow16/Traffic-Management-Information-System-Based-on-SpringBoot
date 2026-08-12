<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input style="width: 240px;margin-right: 5px" v-model="data.name" placeholder="请输入车牌号查询" prefix-icon="Search"></el-input>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="primary" @click="reset">重置</el-button>
    </div>
    <div class="card" style="margin-bottom: 5px">
      <el-button type="primary" @click="handleAdd">新增</el-button>
      <el-button type="warning" @click="delBatch">批量删除</el-button>
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
              {{ scope.row.status === 'UND' ? '举报中' : scope.row.status === 'UNT' ? '未处理' : scope.row.status === 'HPD' ? '已支付' : scope.row.status === 'AED' ? '已申诉' : scope.row.status === 'PAS' ? '申诉成功' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button v-if="scope.row.status !== 'UNT'" @click="handleView(scope.row)" type="success" :icon="Document" circle></el-button>
            <el-button v-if="scope.row.status === 'UNT'" @click="handleUpdate(scope.row)" type="primary" :icon="Edit" circle></el-button>
            <el-button @click="del(scope.row.id)" type="danger" :icon="Delete" circle></el-button>
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
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="data.viewData.status === 'UND' ? 'warning' : data.viewData.status === 'UNT' ? 'danger' : data.viewData.status === 'HPD' ? 'success' : data.viewData.status === 'AED' ? 'info' : data.viewData.status === 'PAS' ? 'success' : 'primary'" size="small">
            {{ data.viewData.status === 'UND' ? '举报中' : data.viewData.status === 'UNT' ? '未处理' : data.viewData.status === 'HPD' ? '已支付' : data.viewData.status === 'AED' ? '已申诉' : data.viewData.status === 'PAS' ? '申诉成功' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.viewVisible = false">关闭</el-button>
          <el-button type="primary" @click="confirmProcessed" v-if="data.viewData.status === 'HPD' || data.viewData.status === 'UND'">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="申诉详情" v-model="data.appealProcessVisible" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车牌号">{{ data.appealData.plate }}</el-descriptions-item>
        <el-descriptions-item label="车主">{{ data.appealData.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="车型">{{ data.appealData.carModel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="违章时间">{{ data.appealData.time }}</el-descriptions-item>
        <el-descriptions-item label="违章地点">{{ data.appealData.address }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ data.appealData.violationName }}</el-descriptions-item>
        <el-descriptions-item label="申诉理由" :span="2">
          <div v-html="data.appealData.appealContent" style="max-height: 200px; overflow-y: auto;"></div>
        </el-descriptions-item>
        <el-descriptions-item label="状态" :span="2">
          <el-tag :type="data.appealData.status === 'UND' ? 'warning' : data.appealData.status === 'UNT' ? 'danger' : data.appealData.status === 'HPD' ? 'success' : data.appealData.status === 'AED' ? 'info' : data.appealData.status === 'PAS' ? 'success' : 'primary'" size="small">
            {{ data.appealData.status === 'UND' ? '举报中' : data.appealData.status === 'UNT' ? '未处理' : data.appealData.status === 'HPD' ? '已支付' : data.appealData.status === 'AED' ? '已申诉' : data.appealData.status === 'PAS' ? '申诉成功' : '已处理' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.appealProcessVisible = false">关闭</el-button>
          <el-button type="danger" @click="handleReject">驳回</el-button>
          <el-button type="primary" @click="handlePass">同意</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog title="驳回申诉" v-model="data.rejectVisible" width="700px" destroy-on-close>
      <div style="border: 1px solid #ccc; width: 100%">
        <Toolbar
            style="border-bottom: 1px solid #ccc"
            :editor="editorRef"
            :defaultConfig="toolbarConfig"
            :mode="'default'"
        />
        <Editor
            style="height: 300px; overflow-y: hidden;"
            v-model="data.rejectForm.reply"
            :defaultConfig="editorConfig"
            :mode="'default'"
            @onCreated="handleCreated"
        />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="data.rejectVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认</el-button>
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
            <el-option v-for="item in data.cars" :label="item.plate" :value="item.plate" :key="item.plate"></el-option>
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

// 编辑器实例，必须用 shallowRef
const editorRef = shallowRef()

const toolbarConfig = {}
const editorConfig = { placeholder: '请输入驳回理由...' }

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
    const editor = editorRef.value
    if (editor == null) return
    editor.destroy()
})

const handleCreated = (editor) => {
    editorRef.value = editor // 记录 editor 实例，重要！
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
  appealProcessVisible: false,
  appealData: {},
  rejectVisible: false,
  rejectForm: {}
})

const formRef = ref()

const load = () => {
  request.get('/violation/selectViolationPage', {
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
      data.from.id ? update() : add()
      data.formVisible = false
    }
  })
}

const add = () => {
  request.post('/violation/policeAdd', data.from).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/violation/update', data.from).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleUpdate = (row) => {
  data.from = JSON.parse(JSON.stringify(row))
  
  // 回显车辆信息
  const car = data.cars.find(item => item.plate === row.plate)
  if (car) {
    data.selectCar = car
  }

  // 回显违章类型信息
  const violationId = data.violationIds.find(item => item.id === row.violationid)
  if (violationId) {
    data.selectViolationId = violationId
  }
  
  data.formVisible = true
}

const handleView = (row) => {
  if (row.status === 'AED') {
    handleAppealProcess(row)
    return
  }
  data.viewData = JSON.parse(JSON.stringify(row))
  const car = data.cars.find(item => item.plate === row.plate)
  if (car) {
    data.viewData.carModel = car.model
  }
  data.viewVisible = true
}

const handleAppealProcess = (row) => {
  data.appealData = JSON.parse(JSON.stringify(row))
  const car = data.cars.find(item => item.plate === row.plate)
  if (car) {
    data.appealData.carModel = car.model
  }
  
  // 获取申诉详情
  request.get('/appealed/selectById/' + row.id).then(res => {
    if (res.code === '200' && res.data) {
      data.appealData.appealContent = res.data.content
      data.appealData.appealId = res.data.id // 虽然id一样，但明确一下
      data.appealProcessVisible = true
    } else {
      ElMessage.warning("未找到申诉信息")
    }
  })
}

const handleReject = () => {
  data.rejectForm = {
    id: data.appealData.id, // 申诉ID即违章ID
    reply: ''
  }
  data.rejectVisible = true
}

const confirmReject = () => {
  if (!data.rejectForm.reply) {
    ElMessage.warning("请输入驳回理由")
    return
  }
  request.put('/appealed/reject', data.rejectForm).then(res => {
    if (res.code === '200') {
      ElMessage.success("已驳回")
      data.rejectVisible = false
      data.appealProcessVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handlePass = () => {
  ElMessageBox.confirm("确认同意该申诉吗？违章状态将变为申诉成功。", "确认同意", { type: 'warning' }).then(() => {
    request.put('/appealed/pass/' + data.appealData.id).then(res => {
      if (res.code === '200') {
        ElMessage.success("操作成功")
        data.appealProcessVisible = false
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(() => {})
}

const confirmProcessed = () => {
  request.put('/violation/policeAffirm', data.viewData).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功")
      data.viewVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const resetFormState = () => {
  data.selectCar = {}
  data.selectViolationId = {}
}


const del = (id) => {
  ElMessageBox.confirm("删除数据后无法恢复，您确认删除吗","删除确认",{ type: 'warning'}).then(()=>{
    request.delete('/violation/deleteById/'+id).then(res => {
      if (res.code === '200') {
        ElMessage.success("操作成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch()
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(row => row.id)
}

const delBatch = () => {
  if (data.ids.length === 0) {
    ElMessage.warning("请选择数据")
    return
  }
  ElMessageBox.confirm("删除数据后无法恢复，您确认删除吗","删除确认",{ type: 'warning'}).then(()=>{
    request.delete('/violation/deleteBatch', {data: data.ids}).then(res => {
      if (res.code === '200') {
        ElMessage.success("操作成功")
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}



</script>



<style scoped>
  .violation-page { padding: 20px }
  .vehicle-info { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px }
  .previews { margin-top: 8px; display:flex; gap:8px; flex-wrap:wrap }
  .preview-img { width: 140px; height: 90px; object-fit:cover; border:1px solid #eaeaea; border-radius:4px }
</style>
