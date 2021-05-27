import request from '@/utils/request'
export default {
    //数据字典列表
    
    dictList(id){
       return request({
        //使用模板字符串取值
        url:`/admin/cmn/dict/findChildData/${id}`,
        //请求方式
        method:"get"
       })
    }
}