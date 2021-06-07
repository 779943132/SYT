<template>
  <div class="app-container">
    <el-form :inline="true" class="demo-form-inline">
      <el-form-item>
        <!-- 双向绑定searchObj.hosname和searchObj.hoscode使传回数据发生改变。调用getList()方法 -->
        <el-input v-model="searchObj.hosname" placeholder="医院名称"/>
      </el-form-item>
      <el-form-item>
        <el-input v-model="searchObj.hoscode" placeholder="医院编号"/>
      </el-form-item>
      <el-button type="primary" icon="el-icon-search" @click="getList()"> 查询 </el-button>
    </el-form>
    <!-- 工具条 -->
    <div>
      <el-button type="danger" size="mini" @click="removeAll()">批量删除</el-button>
    </div>
    <!-- banner列表 -->
    <!-- 在一个列表中，将数据与表格绑定，不需要for循环就可以取回数据，并且 -->
    <!-- @selection-change="handleSelectionChange"每次触发复选框事件就得到数据 -->
    <el-table :data="list" stripe style="width: 100%" @selection-change="handleSelectionChange">
      <!-- 复选框 -->
      <el-table-column type="selection" width="55"/>
      <el-table-column type="index" width="50" label="序号"/>
      <el-table-column prop="hosname" label="医院名称"/>
      <el-table-column prop="hoscode" label="医院编号"/>
      <el-table-column prop="apiUrl" label="api基础路径" width="200"/>
      <el-table-column prop="contactsName" label="联系人姓名"/>
      <el-table-column prop="contactsPhone" label="联系人手机"/>
      <!-- 这是一个整体 -->
      <el-table-column label="状态" width="80">
        <template slot-scope="scope">
          {{ scope.row.status === 1 ? '可用' : '不可用' }}
        </template>
      </el-table-column>

      <el-table-column label="操作" width="280" align="center">
        <template slot-scope="scope">
          <!-- 删除 -->
          <el-button
            type="danger"
            size="mini"
            icon="el-icon-delete"
            @click="removeDataById(scope.row.id)">删除 </el-button>
          <!-- 锁定 -->
          <el-button
            v-if="scope.row.status===1"
            type="info"
            size="mini"
            icon="el-icon-close"
            @click="lockHospSet(scope.row.id,0)">状态锁定 </el-button>
          <!-- 取消锁定 -->
          <el-button
            v-else
            type="primary"
            size="mini"
            icon="el-icon-check"
            @click="lockHospSet(scope.row.id,1)">取消锁定 </el-button>
          <router-link :to="'/hospSet/edit/'+scope.row.id">
            <el-button type="primary" size="mini" icon="el-icon-edit"/>
          </router-link>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      :current-page="current"
      :page-size="limit"
      :total="total"
      style="padding: 30px 0; text-align: center;"
      layout="total, prev, pager, next, jumper"
      @current-change="getList"
    />
  </div>

</template>
<script>
import hospset from '@/api/hospset'
/*
        跨域问题：{
            三个地方，任何一个不同就会产生跨域问题
            访问协议：http,https
            访问地址：
            端口号：
            解决方案：
            1.在controller类上加上一个@CrossOrigin注解，就允许跨域访问
        }
    */
export default {
  // 定义变量和初始值
  data() {
    return {
      current: 1, // 当前页
      limit: 5, // 每页显示的记录数
      searchObj: {}, // 封装的对象
      list: [], // 每页数据集合
      total: 0,
      id: 0, // 删除的id
      selectionData: [] // 复选框得到的数据
    }
  },
  // 页面加载之间
  created() {
    this.getList()
  },
  methods: {
    // 得到复选框的值
    handleSelectionChange(selection) {
      this.selectionData = selection
    },
    // 医院设置列表方法
    getList(page = 1) {
      this.current = page
      // 调用方法
      hospset.getHospSetList(this.current, this.limit, this.searchObj)
        .then(response => {
          // 将数据传到list集合中
          this.list = response.data.records
          // 总记录数
          this.total = response.data.total
        })
        .catch(error => {
          console.log(error)
        })
    },
    removeDataById(index) {
      this.id = index
      this.$confirm('此操作将永久删除该医院设置信息, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 删除
        hospset.delectById(this.id)
          .then(response => {
            // 提示删除成功
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            // 刷新页面
            this.getList()
          })
          .catch()
      })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    removeAll() {
      var idList = []
      // 遍历复选框得到的数据,将得到的数据中id push到ListId中
      for (var i = 0; i < this.selectionData.length; i++) {
        idList.push(this.selectionData[i].id)
      }
      this.$confirm('此操作将永久删除该医院设置信息, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 删除
        hospset.deleteAll(idList)
          .then(response => {
            // 提示删除成功
            this.$message({
              type: 'success',
              message: '删除成功!'
            })
            // 刷新页面
            this.getList()
          })
          .catch()
      })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    // 锁定，与取消锁定
    lockHospSet(id, status) {
      hospset.lockHospitalSet(id, status)
        .then(response => {
          this.getList()
        })
        .catch()
    }
  }
}
</script>
