import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const request = axios.create({
    baseURL: '/api',
    timeout: 30000
})

//request 拦截器
//可以自发送前对请求做一些处理
request.interceptors.request.use(config=>{
    config.headers['Content-Type'] = 'application/json;charset=utf-8'
    //获取登录数据
    let user = localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {}
    config.headers['token'] = user.token   //设置请求头
    return config
},error => {
    return Promise.reject(error)
});

//response 拦截器
//可以在接口响应后同时处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        if (res.code === '401') {
            ElMessage.error('登录已过期，请重新登录')
            router.push('/login')
        }
        return res
    },
    error => {
        if (error.response && error.response.status === 404) {
            ElMessage.error('未找到请求接口')
        } else if (error.response && error.response.status === 500) {
            ElMessage.error('系统异常,请查看后端控制台报错')
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request