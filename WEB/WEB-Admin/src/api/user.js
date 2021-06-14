import request from '@/utils/request'
const api_name = '/admin/user'
export default {
  //分页查询用户列表
  getPageList(page,limit,searchObj){
    return request({
      //使用模板字符串取值
      url:`${api_name}/${page}/${limit}`,
      //请求方式
      method:"post",
      data:searchObj
    })
  },
  // 状态修改
  lock(id, status) {
    return request({
      // 使用模板字符串取值
      url: `${api_name}/lock/${id}/${status}`,
      // 请求方式
      method: 'put'
    })
  },
  //用户详情
  show(id) {
    return request({
      url: `${api_name}/show/${id}`,
      method: 'get'
    })
  },
  //认证审批
  approval(id,authStatus) {
    return request({
      url: `${api_name}/approval/${id}/${authStatus}`,
      method: 'get'
    })
  }
}
