import request from "@/utils/request";
const api_name=`/api/ucenter/wx`
export default {
  //查询医院列表
  getLoginParam(){
    return request({
      url:`${api_name}/getLoginParam`,
      method:'get'
    })
  }
}
