<template>
  <div class="ValiCode disabled-select" style="width: 100%; height: 100%" @click="refreshCode">
    <span v-for="(item, index) in data.codeList" :key="index" :style="getStyle(item)">{{item.code}}</span>
  </div>
</template>

<script setup>
  import {reactive, onMounted} from "vue";

  const emit = defineEmits(['input'])

  const data = reactive({
    length: 4,
    codeList: []
  })

  const createdCode = () => {
    const len = data.length
    const codeList = []
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXZYabcdefghijklmnopqrstuvwxyz123456789'
    const charsLen = chars.length

    for (let i = 0; i < len; i++) {
      //设置单个字符的颜色
      const rgb = [Math.round(Math.random()*220), Math.round(Math.random()*240), Math.round(Math.random()*200)]
      //将生成的字符推入codeList中
      codeList.push({
        code: chars.charAt(Math.floor(Math.random() * charsLen)), //从chars中随机获取字符
        color: `rgb(${rgb})`,
        padding: `${[Math.floor(Math.random() * 10)]}px`, //设置间距
        transform: `rotate(${Math.floor(Math.random() * 90) - Math.floor(Math.random() * 90)}deg)`  //设置字符的旋转度数
      })
    }
    data.codeList = codeList

    //将数据送出
    emit('input', codeList.map(item => item.code).join(''))
  }

  const refreshCode = () => {
    createdCode()
  }

  onMounted(() => {
    createdCode()
  })

  const getStyle = (data) => {
    return `color: ${data.color}; font-size: ${data.fontSize}; padding: ${data.padding}; transform: ${data.transform}`
  }
</script>

<style scoped>
  .ValiCode {
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
  }
  .ValiCode span {
    display: inline-block;
    font-size: 18px;
  }
</style>