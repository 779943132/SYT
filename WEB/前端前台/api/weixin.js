import request from "@/utils/request";
const api_name=`/api/ucenter/wx`
export default {
  //微信登录
  getLoginParam(){
    return request({
      url:`${api_name}/getLoginParam`,
      method:'get'
    })
  },
  //微信支付
  createNative(orderId){
    return request({
      url:`/api/order/weixin/createNative/${orderId}`,
      method:'get'
    })
  },
  //查询支付状态
  queryPayStatus(orderId) {
    return request({
      url: `/api/order/weixin/queryPayStatus/${orderId}`,
      method: 'get'
    })
  }
}
