package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;

    //上传医院接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //将参数map集合转换字符串
        String s = JSONObject.toJSONString(paramMap);
        //再将字符串转化为Hospital对象
        Hospital hospital = JSONObject.parseObject(s, Hospital.class);
        //判断数据是否存在
        String hoscode = hospital.getHoscode();
        Hospital hospital1 = hospitalRepository.getHospitalByHoscode(hoscode);
        //存在修改
        if(hospital1!=null){
            hospital.setStatus(hospital1.getStatus());
            //创建时间
            hospital.setCreateTime(hospital1.getCreateTime());
            //更新时间
            hospital.setUpdateTime(new Date());
            //是否删除
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else {
            //不存在，添加
            hospital.setStatus(0);
            //创建时间
            hospital.setCreateTime(new Date());
            //更新时间
            hospital.setUpdateTime(new Date());
            //是否删除
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
            hospitalRepository.save(hospital);
        }

    }
}
