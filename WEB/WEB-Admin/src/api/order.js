import request from '@/utils/request'
export default {
  //订单列表
  getOrderList(page,limit,orderQueryVo){
    return request({
      //使用模板字符串取值
      url:`/admin/order/orderInfo/getOrderList/${page}/${limit}`,
      //请求方式
      method:"get",
      params : orderQueryVo
    })
  },
  show(id){
    return request({
      //使用模板字符串取值
      url:`/admin/order/orderInfo/getOrders/${id}`,
      //请求方式
      method:"get"
    })
  },
  getStatusList(){
    return request({
      //使用模板字符串取值
      url:`/admin/order/orderInfo/getStatusList`,
      //请求方式
      method:"get"
    })
  },
  deleteOrder(id){
    return request({
      //使用模板字符串取值
      url:`/admin/order/orderInfo/deleteOrders/${id}`,
      //请求方式
      method:"delete"
    })
  },
  getCountMap(obj){
    return request({
      //使用模板字符串取值
      url:`/admin/order/orderInfo/getStatisticsMap`,
      //请求方式
      method:"delete",
      params : obj
    })
  }
}
