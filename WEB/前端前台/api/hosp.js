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
  },
  //根据医院编号和科室编号获取可预约数据
  getBookingScheduleRule(page,limit,hoscode,depcode){
    return request({
      url:`${api_name}/auth/getBookingScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
      method:'get'
    })
  },
  //获取排班数据
  findScheduleList(hoscode,depcode,workDate){
    return request({
      url:`${api_name}/auth/findScheduleList/${hoscode}/${depcode}/${workDate}`,
      method:'get'
    })
  },
  //根据排班id获取排班信息
  getSchedule(id){
    return request({
      url:`${api_name}/getSchedule/${id}`,
      method:'get'
    })
  }



}
