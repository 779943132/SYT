<template>
  <div class="app-container">
    医院设置添加
    <el-form label-width="120px">
      <el-form-item label="医院名称">
        <el-input v-model="hospitalSet.hosname"/>
      </el-form-item>
      <el-form-item label="医院编号">
        <el-input v-model="hospitalSet.hoscode"/>
      </el-form-item>
      <el-form-item label="api基础路径">
        <el-input v-model="hospitalSet.apiUrl"/>
      </el-form-item>
      <el-form-item label="联系人姓名">
        <el-input v-model="hospitalSet.contactsName"/>
      </el-form-item>
      <el-form-item label="联系人手机">
        <el-input v-model="hospitalSet.contactsPhone"/>
      </el-form-item>
      <el-form-item>
        <el-button v-if="hospitalSet.id==null" type="primary" @click="save()">保存</el-button>
        <el-button v-else type="primary" @click="update()">修改</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>
<script>
import hospset from '@/api/hospset'

export default {
  // 定义变量和初始值
  data() {
    return {
      hospitalSet: {},
      id: 0
    }
  },
  // 页面加载之前执行
  created() {
    // 判断路由中是否有id数据
    if (this.$route.params && this.$route.params.id) {
      this.id = this.$route.params.id
      this.getHospSetById(this.id)
    } else {
      this.hospitalSet = {}
    }
  },
  methods: {
    // 添加和修改操作
    save() {
      // 添加
      hospset.saveHospitalSet(this.hospitalSet)
        .then(response => {
          this.$message({
            type: 'success',
            message: '添加成功!'
          })
          // 路由跳转到list页面
          this.$router.push({ path: '/hospSet/list' })
        })
        .catch()
    },
    // 修改
    update() {
      hospset.updateHospitalSet(this.hospitalSet)
        .then(response => {
          this.$message({
            type: 'success',
            message: '修改成功!'
          })
          // 路由跳转到list页面
          this.$router.push({ path: '/hospSet/list' })
        })
        .catch(response => {
          this.$message({
            type: 'success',
            message: '修改失败!'
          })
        })
    },
    // 查询
    getHospSetById() {
      hospset.getHospSet(this.id)
        .then(response => {
          this.hospitalSet = response.data
        })
        .catch()
    }
  }
}
</script>
