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
              <el-button @click="handleCheck(scope.row)" type="success" :icon="Document" circle></el-button>
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

    <el-dialog title="查看车辆信息" v-model="data.checkVisible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车辆照片" :span="2">
          <div style="display: flex; justify-content: center;">
            <el-image
                style="width: 100px; height: 100px; border-radius: 50%"
                :src="data.car.avatar"
                :preview-src-list="[data.car.avatar]"
            />
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ data.car.plate }}</el-descriptions-item>
        <el-descriptions-item label="车架号(VIN)">{{ data.car.vin }}</el-descriptions-item>
        <el-descriptions-item label="车辆类型">
          {{ data.models.find(v => v.value === data.car.model) ? data.models.find(v => v.value === data.car.model).label : '其他' }}
        </el-descriptions-item>
        <el-descriptions-item label="发动机号">{{ data.car.engine }}</el-descriptions-item>
        <el-descriptions-item label="品牌">
          {{ data.brands.find(v => v.value === data.car.brand) ? data.brands.find(v => v.value === data.car.brand).label : '其他' }}
        </el-descriptions-item>
        <el-descriptions-item label="颜色">{{ data.car.color }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ data.car.markTime }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click= "data.checkVisible = false">关闭</el-button>
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
              action="/api/files/upload"
              list-type="picture"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
          >
            <el-button type="primary">上传照片</el-button>
          </el-upload>
        </div>
      </div>
      <el-form :model="data.car" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="车牌" prop="plate">
          <el-input v-model="data.car.plate" />
        </el-form-item>
        <el-form-item label="Vin" prop="vin">
          <el-input v-model="data.car.vin"/>
        </el-form-item>
        <el-form-item label="发动机号" prop="engine">
          <el-input v-model="data.car.engine"/>
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
import {Delete, Edit, Search, Document} from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage, ElMessageBox} from "element-plus";

const formRef = ref(null)

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
})

const load = () => {
  request.get('/user/selectCarPage', {
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
  formRef.value.validate((valid) => {
    if (valid) {
      data.car.ownerId = data.user.idCard
      data.car.ownerName = data.user.name
      request.post('/car/add', data.car).then(res => {
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

const handleAvatarSuccess = (res) => {
  data.car.avatar = res.data
}

const handleCheck = (row) => {
  data.car = JSON.parse(JSON.stringify(row))
  data.checkVisible = true
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
