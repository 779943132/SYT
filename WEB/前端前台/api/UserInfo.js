import request  from "../utils/request";

const api_name=`/api/user/`
export default {

  //根据医院编号查询科室信息
  login(userInfo){
    return request({
      url:`${api_name}/login`,
      method:'post',
      data: userInfo
    })
  },
  //邮件验证码发送
  sendCode(email){
    return request({
      url:`${api_name}/sendCode/${email}`,
      method:'get'
    })
  },
  //用户认证接口
  saveUserAuth(userAuth){
    return request({
      url:`${api_name}/auth/userAuth`,
      method:'post',
      data: userAuth
    })
  },
  //根据userid获取用户id
  getUserInfo(){
    return request({
      url:`${api_name}/auth/getUserInfo`,
      method:'get'
    })
  }
}
