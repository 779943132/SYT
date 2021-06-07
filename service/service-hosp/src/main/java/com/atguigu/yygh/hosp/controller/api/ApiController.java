package com.atguigu.yygh.hosp.controller.api;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/hosp")
public class ApiController {
    //医院信息
    @Autowired
    private HospitalService hospitalService;
    //医院设置
    @Autowired
    private HospitalSetService hospitalSetService;
    //科室信息
    @Autowired
    private DepartmentService departmentService;
    //排班信息
    @Autowired
    private ScheduleService scheduleService;
    //得到医院信息
    @PostMapping("saveHospital")
    public Result saveHosp(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院签名,签名进行MD5加密
        String sign = (String)paramMap.get("sign");
        //根据传递医院编号，查询数据库签名
        String hoscode = (String)paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对signkey加密
        String encrypt = MD5.encrypt(signKey);
        if(sign.equals(encrypt)){
            //传输过程将+号变成了“ ”,所以要转回来
            String logData = (String)paramMap.get("logoData");
            logData = logData.replaceAll(" ","+");
            paramMap.put("logoData",logData);
            hospitalService.save(paramMap);
            return Result.ok();
        }else {
            throw  new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
    //查询医院接口
    @PostMapping("hospital/show")
    public Result getHospital(HttpServletRequest request) {
        //获取传递过来医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院编号
        String hoscode = (String) paramMap.get("hoscode");
        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2 根据传递过来医院编码，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if (!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }

        //调用service方法实现根据医院编号查询
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }
    //上传科室信息
    @PostMapping("saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院签名,签名进行MD5加密
        String sign = (String)paramMap.get("sign");
        //根据传递医院编号，查询数据库签名
        String hoscode = (String)paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对signkey加密
        String encrypt = MD5.encrypt(signKey);
        if(sign.equals(encrypt)){
            departmentService.save(paramMap);
            return Result.ok();
        }else {
            throw  new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
    //查询科室信息
    @PostMapping("department/list")
    public Result getDepartmentList(HttpServletRequest request){
        //获取传递过来医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //当前页
        int page = StringUtils.isEmpty(paramMap.get("page"))?1:Integer.parseInt((String)paramMap.get("page"));
        //每页记录数
        int limit = StringUtils.isEmpty(paramMap.get("limit"))?1:Integer.parseInt((String)paramMap.get("limit"));
        //医院编号
        String hoscode = (String) paramMap.get("hoscode");

        //
        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2 根据传递过来医院编码，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if (!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        DepartmentQueryVo dqv = new DepartmentQueryVo();
        dqv.setHoscode(hoscode);
        //调用service方法实现根据医院编号查询
        Page<Department> departmentslist = departmentService.getDepartmentList(dqv,page,limit);
        return Result.ok(departmentslist);
    }
    //删除科室
    @PostMapping("department/remove")
    public Result remove(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院签名,签名进行MD5加密
        String sign = (String)paramMap.get("sign");
        //根据传递医院编号，查询数据库签名
        String hoscode = (String)paramMap.get("hoscode");
        String depcode = (String)paramMap.get("depcode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对signkey加密
        String encrypt = MD5.encrypt(signKey);
        if(sign.equals(encrypt)){
            departmentService.remove(hoscode,depcode);
            return Result.ok();
        }else {
            throw  new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
    //上传排班信息
    @PostMapping("saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院签名,签名进行MD5加密
        String sign = (String)paramMap.get("sign");
        //根据传递医院编号，查询数据库签名
        String hoscode = (String)paramMap.get("hoscode");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对signkey加密
        String encrypt = MD5.encrypt(signKey);
        if(sign.equals(encrypt)){
            scheduleService.save(paramMap);
            return Result.ok();
        }else {
            throw  new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
    //查询排班信息
    @PostMapping("schedule/list")
    public Result getScheduleList(HttpServletRequest request){
        //获取传递过来医院信息
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //当前页
        int page = StringUtils.isEmpty(paramMap.get("page"))?1:Integer.parseInt((String)paramMap.get("page"));
        //每页记录数
        int limit = StringUtils.isEmpty(paramMap.get("limit"))?1:Integer.parseInt((String)paramMap.get("limit"));
        //医院编号
        String hoscode = (String) paramMap.get("hoscode");

        //
        //1 获取医院系统传递过来的签名,签名进行MD5加密
        String hospSign = (String) paramMap.get("sign");

        //2 根据传递过来医院编码，查询数据库，查询签名
        String signKey = hospitalSetService.getSignKey(hoscode);

        //3 把数据库查询签名进行MD5加密
        String signKeyMd5 = MD5.encrypt(signKey);

        //4 判断签名是否一致
        if (!hospSign.equals(signKeyMd5)) {
            throw new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
        ScheduleQueryVo dqv = new ScheduleQueryVo();
        dqv.setHoscode(hoscode);
        //调用service方法实现根据医院编号查询
        Page<Schedule> departmentslist = scheduleService.getScheduleList(dqv,page,limit);
        return Result.ok(departmentslist);
    }
    //删除排班
    @PostMapping("schedule/remove")
    public Result scheduleRemove(HttpServletRequest request){
        Map<String, String[]> requestMap = request.getParameterMap();
        Map<String, Object> paramMap = HttpRequestHelper.switchMap(requestMap);
        //获取医院签名,签名进行MD5加密
        String sign = (String)paramMap.get("sign");
        //根据传递医院编号，查询数据库签名
        String hoscode = (String)paramMap.get("hoscode");
        String hosscheduleid = (String)paramMap.get("hosScheduleId");
        String signKey = hospitalSetService.getSignKey(hoscode);
        //对signkey加密
        String encrypt = MD5.encrypt(signKey);
        if(sign.equals(encrypt)){
            scheduleService.remove(hoscode,hosscheduleid);
            return Result.ok();
        }else {
            throw  new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }
}
