import request from '@/utils/request'

export default {
  // 医院设置路由设置
  getHospSetList(curent, limit, searchObj) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/findPage/${curent}/${limit}`,
      // 请求方式
      method: 'post',
      // 使用json形式传递
      data: searchObj
    })
  },
  delectById(id) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/hospitalSet/${id}`,
      // 请求方式
      method: 'delete'
    })
  },
  deleteAll(idList) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/batchRemove`,
      // 请求方式
      method: 'delete',
      data: idList
    })
  },
  // 状态修改
  lockHospitalSet(id, status) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/lockHospitalSet/${id}/${status}`,
      // 请求方式
      method: 'put'
    })
  },
  // 添加医院设置
  saveHospitalSet(hospitalSet) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/saveHospitalSet`,
      // 请求方式
      method: 'post',
      data: hospitalSet
    })
  },
  // 修改医院设置
  updateHospitalSet(hospitalSet) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/updateHospitalSet`,
      // 请求方式
      method: 'post',
      data: hospitalSet
    })
  },
  // 根据id查询医院设置
  getHospSet(id) {
    return request({
      // 使用模板字符串取值
      url: `/admin/hosp/hospitalSet/getHospSet/${id}`,
      // 请求方式
      method: 'get'
    })
  }

}
