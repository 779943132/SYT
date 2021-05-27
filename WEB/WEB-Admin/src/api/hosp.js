import request from '@/utils/request'
export default {
    //医院列表
    getHospList(page,limit,searchObj){
       return request({
        //使用模板字符串取值
        url:`admin/hosp/hospital/list/${page}/${limit}`,
        //请求方式
        method:"get",
        params: searchObj
       })
    },
    //根据dictcode获取子节点
    findByDictCode(dictCode){
        return request({
         //使用模板字符串取值
         url:`/admin/cmn/dict/findByDictCode/${dictCode}`,
         //请求方式
         method:"get"
        })
     },
     //根据id获取子节点
    findChildData(id){
        return request({
         //使用模板字符串取值
         url:`/admin/cmn/dict/findChildData/${id}`,
         //请求方式
         method:"get"
        })
     },
     updateHospStatus(id,status){
      return request({
         //使用模板字符串取值
         url:`admin/hosp/hospital/updateHospStatus/${id}/${status}`,
         //请求方式
         method:"get"
        })
     },
     getHospShow(id){
      return request({
         //使用模板字符串取值
         url:`admin/hosp/hospital/showHospDetail/${id}`,
         //请求方式
         method:"get"
        })
     },
     getDeptByHoscode(hoscode){
      return request({
         //使用模板字符串取值
         url:`admin/hosp/department/getDeptList/${hoscode}`,
         //请求方式
         method:"get"
        })
     },
     getScheduleRule(page, limit, hoscode, depcode){
      return request({
         //使用模板字符串取值
         url:`admin/hosp/schedule/getScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
         //请求方式
         method:"get"
        })
     },
     getScheduleDetail(hoscode,depcode,workDate){
      return request({
         //使用模板字符串取值
         url:`admin/hosp/schedule/getScheduleDetail/${hoscode}/${depcode}/${workDate}`,
         //请求方式
         method:"get"
        })
     }
}