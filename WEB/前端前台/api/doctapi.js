import request  from "../utils/request";

const api_name=`/admin/cmn/dict`
export default {
  //查询医院列表
  findByDictCode(dictCode){
    return request({
      url:`${api_name}/findByDictCode/${dictCode}`,
      method:'get'
    })
  },
  //根据医院名称进行模糊查询
  findChildData(id){
    return request({
      url:`${api_name}/findChildData/${id}`,
      method:'get'
    })
  }
}
