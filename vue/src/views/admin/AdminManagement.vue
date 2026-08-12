<template>
  <div>
    <div class="card" style="margin-bottom: 5px">
      <el-input style="width: 240px;margin-right: 5px" v-model="data.name" placeholder="请输入真实姓名查询" prefix-icon="Search"></el-input>
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
        <el-table-column label="头像">
          <template #default="scope">
            <img v-if="scope.row.avatar" :src="scope.row.avatar" style="display: block; width: 40px; height: 40px; border-radius: 50%">
          </template>
        </el-table-column>>
        <el-table-column label="账号名" prop="username" />
        <el-table-column label="密码" prop="password" />
        <el-table-column label="真实姓名" prop="name" />
        <el-table-column label="账号状态" prop="status" >
          <template #default="scope">
            <el-tag :type="scope.row.status === 'NOR' ? 'success' : scope.row.status === 'SQU' ? 'warning' : 'danger'" size="small">
              {{ scope.row.status === 'NOR' ? '正常' : scope.row.status === 'SQU' ? '违章' : '封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button @click="handleUpdate(scope.row)" type="primary" :icon="Edit" circle></el-button>
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

    <el-dialog  title="用户信息" v-model="data.formVisible" width="500" destroy-on-close>
      <el-form ref="formRef" :rules="data.rules" :model="data.from" style="padding-top: 20px">
<!--        上传用户头像-->
        <div style="display: flex; flex-direction: column; align-items: center; margin-bottom: 20px">
          <el-avatar
              :size="100"
              :src="data.from.avatar"
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

        <el-form-item label="账号" label-width="80px" prop="username">
          <el-input v-model="data.from.username" autocomplete="off" placeholder="用户名"/>
        </el-form-item>

        <el-form-item label="密码" label-width="80px" prop="password" >
          <el-input v-model="data.from.password" autocomplete="off" placeholder="密码" show-password/>
        </el-form-item>

        <el-form-item label="真实姓名" label-width="80px" prop="name" >
          <el-input v-model="data.from.name" autocomplete="off" placeholder="真实姓名"/>
        </el-form-item>

        <el-form-item label="账号状态" label-width="80px" prop="email" >
          <el-select v-model="data.from.status">
            <el-option v-for="item in data.status" :value="item.value" :label="item.label" :key="item.value"></el-option>
          </el-select>
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
import {reactive, ref} from "vue";
import {Delete, Edit, Search} from "@element-plus/icons-vue"
import request from "@/utils/request.js";
import {ElMessage, ElMessageBox} from "element-plus";

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
    username: [
      { required: true, message: '请输入用户名', trigger: 'blur' },
      { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度应在6-20个字符之间', trigger: 'blur' }
    ],
    name: [
      { required: true, message: '请输入真实姓名', trigger: 'blur' },
      { min: 2, max: 20, message: '真实姓名长度应在2-20个字符之间', trigger: 'blur' }
    ]
  },
  departmentList: [],
  status:[
    {value:"NOR", label:"正常"},
    {value:"OUT", label:"注销"}
  ]
})



const formRef = ref()

const load = () => {
  request.get('/admin/selectPage', {
      params: {
        pageNum: data.pageNum,
        pageSize: data.pageSize,
        name: data.name
      }
    }).then(res => {
      data.tableData = res.data.list
      data.total = res.data.total
  })
}

load()

request.get('/admin/selectAll').then(res => {
  data.departmentList = res.data
})

const handleAvatarSuccess = (res) => {
  if (res.code === '200') {
    data.from.avatar = res.data
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
  data.from = {}
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
  request.post('/admin/add', data.from).then(res => {
    if (res.code === '200') {
      ElMessage.success("操作成功")
      load()
    } else {
      ElMessage.error(res.msg)
    }
  })
}

const update = () => {
  request.put('/admin/update', data.from).then(res => {
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
  data.formVisible = true
}

const del = (id) => {
  ElMessageBox.confirm("删除数据后无法恢复，您确认删除吗","删除确认",{ type: 'warning'}).then(()=>{
    request.delete('/admin/deleteById/'+id).then(res => {
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
    request.delete('/admin/deleteBatch', {data: data.ids}).then(res => {
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

</style>
