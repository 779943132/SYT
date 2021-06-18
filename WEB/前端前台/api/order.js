import request  from "../utils/request";

const api_name=`/api/order/orderInfo`
export default {
  //生成订单
  submitOrder(scheduleId,patientId){
    return request({
      url:`${api_name}/auth/submitOrder/${scheduleId}/${patientId}`,
      method:'post'
    })
  },
  //根据订单id得到订单详情信息
  getOrders(orderId) {
    return request({
      url: `${api_name}/auth/getOrders/${orderId}`,
      method: 'get'
    })
  },
  //订单列表
  getOrderList(page,limit,OrderQueryVo) {
    return request({
      url: `${api_name}/auth/getOrderList/${page}/${limit}`,
      method: 'get',
      params : OrderQueryVo
    })
  },
  //查询订单状态
  getStatusList() {
    return request({
      url: `${api_name}/auth/getStatusList`,
      method: 'get'
    })
  },
  //取消订单
  cancelOrder(orderId){
    return request({
      url: `${api_name}/auth/cancelOrder/${orderId}`,
      method: 'get'
    })
  }
}
