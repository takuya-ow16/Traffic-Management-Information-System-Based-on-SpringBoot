<template>
  <div>
    <el-card>
      <div class="card" style="margin-bottom: 5px">
        <el-input style="width: 240px;margin-right: 5px" v-model="data.name" placeholder="请输入名称查询" prefix-icon="Search"></el-input>
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
          <el-table-column label="照片">
            <template #default="scope">
              <img v-if="scope.row.avatar" :src="scope.row.avatar" style="display: block; width: 40px; height: 40px; border-radius: 50%">
            </template>
          </el-table-column>
          <el-table-column label="车牌号" prop="plate" />
          <el-table-column label="车型" prop="model">
            <template #default="scope">
              {{ data.models.find(v => v.value === scope.row.model) ? data.models.find(v => v.value === scope.row.model).label : '其他' }}
            </template>
          </el-table-column>
          <el-table-column label="品牌" prop="brand">
            <template #default="scope">
              {{ data.brands.find(v => v.value === scope.row.brand) ? data.brands.find(v => v.value === scope.row.brand).label : '其他' }}
            </template>
          </el-table-column>
          <el-table-column label="颜色" prop="color" />
          <el-table-column label="车辆状态" prop="status" >
            <template #default="scope">
              <el-tag :type="scope.row.status === 'NOR' ? 'success' : scope.row.status === 'SQU' ? 'warning' : scope.row.status === 'TBC' ? 'primary' : 'danger'" size="small">
                {{ scope.row.status === 'NOR' ? '正常' : scope.row.status === 'SQU' ? '违章' : scope.row.status === 'TBC' ? '待确认' : '注销' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button @click="handleCheck(scope.row)" type="primary" :icon="Edit" circle></el-button>
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
    </el-card>

    <el-dialog title="编辑车辆信息" v-model="data.editVisible" width="500px">
      <el-form :model="data.car" :rules="rules" ref="editFormRef" label-width="80px">

        <div style="display: flex; flex-direction: column; align-items: center; margin-bottom: 20px">
          <el-avatar
              :size="100"
              :src="data.car.avatar"
              style="margin-bottom: 10px;"
          />

          <el-upload
              action="/api/files/upload"
              list-type="picture"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">上传照片</el-button>
          </el-upload>
        </div>

        <el-form-item label="车牌" prop="plate">
          <el-input v-model="data.car.plate"/>
        </el-form-item>
        <el-form-item label="Vin" prop="vin">
          <el-input v-model="data.car.vin" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="data.car.model">
            <el-option v-for="item in data.models" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="发动机号" prop="engine">
          <el-input v-model="data.car.engine"/>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-input v-model="data.car.markTime"/>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="data.car.brand">
            <el-option v-for="item in data.brands" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-select v-model="data.car.color">
            <el-option v-for="item in data.colors" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="data.car.status">
            <el-option v-for="item in data.status" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click= "data.editVisible = false">关闭</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>


    
    <el-dialog title="查看车辆信息" v-model="data.checkVisible" width="500px">
      <el-form :model="data.car" label-width="80px">

        <el-form-item>
          <div style="width: 100%; display: flex; justify-content: center; align-items: center;">
            <el-avatar
                :size="100"
                :src="data.car.avatar"
            />
          </div>
        </el-form-item>

        <el-form-item label="车牌">
          <el-input v-model="data.car.plate" disabled/>
        </el-form-item>
        <el-form-item label="Vin">
          <el-input v-model="data.car.vin" disabled/>
        </el-form-item>
        <el-form-item label="类型">
          <el-input :model-value="data.models.find(v => v.value === data.car.model) ? data.models.find(v => v.value === data.car.model).label : '其他'" disabled/>
        </el-form-item>
        <el-form-item label="发动机号">
          <el-input v-model="data.car.engine" disabled/>
        </el-form-item>
        <el-form-item label="车主名">
          <el-input v-model="data.car.ownerName" disabled/>
        </el-form-item>
        <el-form-item label="车主身份证">
          <el-input v-model="data.car.ownerId" disabled/>
        </el-form-item>
        <el-form-item label="注册时间">
          <el-input v-model="data.car.markTime" disabled/>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input :model-value="data.brands.find(v => v.value === data.car.brand) ? data.brands.find(v => v.value === data.car.brand).label : '其他'" disabled/>
        </el-form-item>
        <el-form-item label="颜色">
          <el-input v-model="data.car.color" disabled/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click= "data.checkVisible = false">关闭</el-button>
        <el-button type="primary" @click="confirm" v-if="data.car.status === 'TBC'">确认</el-button>
      </template>
    </el-dialog>

    <el-dialog title="新增车辆信息" v-model="data.addVisible" width="500px">
      <div style="width: 100%; display: flex; justify-content: center; align-items: center; margin-bottom: 20px;">
        <div style="display: flex; flex-direction: column; align-items: center;">
          <el-avatar
              :size="100"
              :src="data.car.avatar"
              style="margin-bottom: 10px;"
          />

          <el-upload
              action="http://localhost:9090/files/upload"
              list-type="picture"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">上传照片</el-button>
          </el-upload>
        </div>
      </div>
      <el-form :model="data.car" :rules="rules" ref="addFormRef" label-width="80px">
        <el-form-item label="车牌" prop="plate">
          <el-input v-model="data.car.plate" />
        </el-form-item>
        <el-form-item label="Vin" prop="vin">
          <el-input v-model="data.car.vin"/>
        </el-form-item>
        <el-form-item label="发动机号" prop="engine">
          <el-input v-model="data.car.engine"/>
        </el-form-item>
        <el-form-item label="车主身份证" style="margin-bottom: 25px" prop="ownerId">
          <el-input v-model="data.car.ownerId"/>
        </el-form-item>
        <el-form-item label="品牌">
          <el-select v-model="data.car.brand">
            <el-option v-for="item in data.brands" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="颜色">
          <el-select v-model="data.car.color">
            <el-option v-for="item in data.colors" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="data.car.model">
            <el-option v-for="item in data.models" :key="item.value" :label="item.label" :value="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click= "data.addVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {Delete, Edit, Search} from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const addFormRef = ref(null)
const editFormRef = ref(null)

const rules = reactive({
  plate: [
    { required: true, message: '请输入车牌号', trigger: 'blur' },
    { pattern: /^[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]$/, message: '请输入正确的车牌号', trigger: 'blur' }
  ],
  vin: [
    { required: true, message: '请输入VIN码', trigger: 'blur' },
    { pattern: /^[A-HJ-NPR-Z0-9]{17}$/, message: '请输入正确的17位VIN码', trigger: 'blur' }
  ],
  engine: [
    { required: true, message: '请输入发动机号', trigger: 'blur' },
    { pattern: /^[A-Z0-9]{6,16}$/, message: '请输入正确的发动机号', trigger: 'blur' }
  ],
  ownerId: [
    { required: true, message: '请输入车主身份证号', trigger: 'blur' },
    { pattern: /^[1-9]\d{5}(18|19|20)\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]$/, message: '请输入正确的身份证号', trigger: 'blur' }
  ]
})

const data = reactive({
  user: JSON.parse(localStorage.getItem('user') || '{}'), //从token中获取车辆信息
  car: {}, //用于存储车辆信息
  addVisible: false,
  tableData:[],
  total: 0,
  pageNum:1,
  pageSize: 10,
  name: '',
  ids: [],
  checkVisible: false,
  editVisible: false,
  colors: [
    {label: "白色", value: "白色"},
    {label: "黑色", value: "黑色"},
    {label: "灰色", value: "灰色"},
    {label: "银色", value: "银色"},
    {label: "红色", value: "红色"},
    {label: "蓝色", value: "蓝色"},
    {label: "棕色", value: "棕色"},
    {label: "绿色", value: "绿色"},
    {label: "黄色", value: "黄色"},
    {label: "橙色", value: "橙色"},
    {label: "紫色", value: "紫色"},
    {label: "香槟色", value: "香槟色"},
    {label: "其他", value: "其他"}
  ],
  brands:[
    {label: "大众", value: "VW"},
    {label: "奥迪", value: "AUDI"},
    {label: "梅赛德斯-奔驰", value: "MB"},
    {label: "宝马", value: "BMW"},
    {label: "丰田", value: "TOYOTA"},
    {label: "本田", value: "HONDA"},
    {label: "日产", value: "NISSAN"},
    {label: "福特", value: "FORD"},
    {label: "雪佛兰", value: "CHEV"},
    {label: "别克", value: "BUICK"},
    {label: "现代", value: "HYUNDAI"},
    {label: "起亚", value: "KIA"},
    {label: "特斯拉", value: "TESLA"},
    {label: "标致", value: "PEUGEOT"},
    {label: "雪铁龙", value: "CITROEN"},
    {label: "沃尔沃", value: "VOLVO"},
    {label: "捷豹", value: "JAGUAR"},
    {label: "路虎", value: "LAND ROVER"},
    {label: "保时捷", value: "PORSCHE"},
    {label: "比亚迪", value: "BYD"},
    {label: "吉利", value: "GEELY"},
    {label: "长城汽车", value: "GWM"},
    {label: "哈弗", value: "HAVAL"},
    {label: "坦克", value: "TANK"},
    {label: "长安", value: "CHANGAN"},
    {label: "奇瑞", value: "CHERY"},
    {label: "蔚来", value: "NIO"},
    {label: "理想汽车", value: "LI"},
    {label: "小鹏汽车", value: "XPENG"},
    {label: "红旗", value: "HONGQI"},
    {label: "五菱", value: "WULING"},
    {label: "领克", value: "LYNK & CO"},
    {label: "极氪", value: "ZEEKR"}
  ],
  models: [
      {label: "大型客车", value: "DXKC"},
      {label: "中型客车", value: "ZXKC"},
      {label: "小型客车", value: "XXKC"},
      {label: "微型客车", value: "WXKC"},
      {label: "重型货车", value: "ZXHC"},
      {label: "中型货车", value: "ZXHC"},
      {label: "轻型货车", value: "QXHC"},
      {label: "微型货车", value: "WXHC"}
  ],
  status: [//正常(NOR)，违章(SQU)，注销(OUT)，待确认(TBC)
    {label: "正常", value: "NOR"},
    {label: "违章", value: "SQU"},
    {label: "注销", value: "OUT"},
    {label: "待确认", value: "TBC"},
  ]
})

const load = () => {
  request.get('/car/selectPage', {
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

const handleAdd = () => {
  data.car = {}
  data.addVisible = true
}

const save = () => {
  if (data.car.id) {
    editFormRef.value.validate((valid) => {
      if (valid) {
        request.put('/car/update', data.car).then(res => {
          if (res.code === '200') {
            ElMessage.success('更新成功')
            data.editVisible = false
            load()
          } else {
            ElMessage.error(res.msg)
          }
        })
      }
    })
  } else {
    addFormRef.value.validate((valid) => {
      if (valid) {
        request.post('/car/addPol', data.car).then(res => {
          if (res.code === '200') {
            ElMessage.success('保存成功')
            data.addVisible = false
            load()
          } else {
            ElMessage.error(res.msg)
          }
        })
      }
    })
  }
}

const confirm = () => {
  data.car.status = 'NOR'
  request.put('/car/update', data.car).then(res => {
    if (res.code === '200') {
      ElMessage.success('确认成功')
      data.checkVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const handleAvatarSuccess = (res) => {
  data.car.avatar = res.data
}

const handleCheck = (row) => {
  data.car = JSON.parse(JSON.stringify(row))
  if (data.car.status === 'TBC') {
    data.checkVisible = true
  } else {
    data.editVisible = true
  }
}

// const handleUpdate = (row) => {
//   data.car = JSON.parse(JSON.stringify(row))
//   data.addVisible = true
// }

const del = (id) => {
  ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' }).then(() => {
    request.delete('/car/deleteById/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}

const handleSelectionChange = (rows) => {
  data.ids = rows.map(v => v.id)
}

const delBatch = () => {
  if (!data.ids.length) {
    ElMessage.warning('请选择数据')
    return
  }
  ElMessageBox.confirm('确认批量删除?', '提示', { type: 'warning' }).then(() => {
    request.delete('/car/deleteBatch', { data: data.ids }).then(res => {
      if (res.code === '200') {
        ElMessage.success('删除成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  })
}

const reset = () => {
  data.name = ''
  load()
}

load()
</script>

<style scoped>
</style>
