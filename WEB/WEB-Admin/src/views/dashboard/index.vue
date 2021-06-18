<template>
<!--  <div class="dashboard-container">
    <div class="dashboard-text">姓名:{{ name }}</div>
    <div class="dashboard-text">角色:<span v-for="role in roles" :key="role">{{ role }}</span></div>
    <div class="dashboard-text">邮箱:{{ email }}</div>
  </div>-->
  <div class="app-container">
    <h4>管理员信息</h4>
    <table class="table table-striped table-condenseda table-bordered" width="100%">
      <tbody>
      <tr>
        <th width="15%">姓名</th>
        <td width="35%"><b style="font-size: 14px">{{ name }}</b> </td>
        <th width="15%">头像</th>
        <td width="35%">

          <el-upload
            class="avatar-uploader"
            action="http://localhost/api/oss/file/fileUpload"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img :src="avatar" width="80">
            <img v-if="imageUrl" :src="imageUrl" class="avatar">
            <i v-else class="el-icon-plus avatar-uploader-icon"> 更换头像</i>
          </el-upload>
        </td>
      </tr>
      <tr>
        <th>角色</th>
        <td>{{roles}}</td>
        <th>邮箱</th>
        <td>{{email}}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import user from "../../api/user";

export default {
  name: 'Dashboard',
  computed: {
    ...mapGetters([
      'name',
      'roles',
      'email',
      'avatar',
      'token'
    ])
  },
  data() {
    return {
      imageUrl: '',
      AvatarVo:{}
    };
  },
  methods:{
    handleAvatarSuccess(res, file) {
      this.imageUrl = URL.createObjectURL(file.raw);
      this.AvatarVo.url =file.response.data
      this.AvatarVo.token =this.token
      user.updateUserAvatar(this.AvatarVo).then(response=>{
        this.$router.go(0)
      })
    },
    beforeAvatarUpload(file) {
      const isLt2M = file.size / 1024 / 1024 < 2;
      if (!isLt2M) {
        this.$message.error('上传头像图片大小不能超过 2MB!');
      }
      return isLt2M;
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
  }
  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 200px;
  height: 80px;
  line-height: 80px;
  text-align: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
</style>
