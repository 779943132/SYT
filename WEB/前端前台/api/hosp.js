import request  from "../utils/request";

const api_name=`/api/hosp/hospital`
export default {
  //查询医院列表
  getPageList(page,limit,searchObj){
      return request({
          url:`${api_name}/findHospList/${page}/${limit}`,
          method:'get',
          params: searchObj
      })
  },
  //根据医院名称进行模糊查询
  getByHosname(hosname){
    return request({
      url:`${api_name}/findByHosName/${hosname}`,
      method:'get'
    })
  },
  //根据医院编号查询详情信息
  show(hosname){
    return request({
      url:`${api_name}/findHospDetail/${hosname}`,
      method:'get'
    })
  },
  //根据医院编号查询科室信息
  findDepartment(hosname){
    return request({
      url:`${api_name}/department/${hosname}`,
      method:'get'
    })
  }

}
