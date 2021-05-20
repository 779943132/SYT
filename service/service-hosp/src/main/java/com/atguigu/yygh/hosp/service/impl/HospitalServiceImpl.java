package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.hosp.repository.HospitalRepository;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class HospitalServiceImpl implements HospitalService {
    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private DictFeignClient dictFeignClient;
    //上传医院接口
    @Override
    public void save(Map<String, Object> paramMap) {
        //将参数map集合转换字符串
        String s = JSONObject.toJSONString(paramMap);
        //再将字符串转化为Hospital对象
        Hospital hospital = JSONObject.parseObject(s, Hospital.class);
        //判断数据是否存在
        String hoscode = hospital.getHoscode();
        Hospital mongoDataHospital = hospitalRepository.getHospitalByHoscode(hoscode);
        //存在修改
        if(mongoDataHospital!=null){
            mongoDataHospital.setStatus(mongoDataHospital.getStatus());
            //创建时间
            mongoDataHospital.setCreateTime(mongoDataHospital.getCreateTime());
            //更新时间
            mongoDataHospital.setUpdateTime(new Date());
            //是否删除
            mongoDataHospital.setIsDeleted(0);
            hospitalRepository.save(mongoDataHospital);
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

    @Override
    public Hospital getByHoscode(String hoscode) {
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        return hospital;
    }
    //条件查询带分页
    @Override
    public Page<Hospital> selectHospPage(int page, int limit, HospitalQueryVo hospitalQueryVo) {
        Pageable pageable = PageRequest.of(page-1,limit);
        //创建Example对象,模糊查询，忽略大小写
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Hospital Hospital = new Hospital();
        //将vo中数据传到正常对象中
        BeanUtils.copyProperties(hospitalQueryVo,Hospital);
        Hospital.setIsDeleted(0);
        Example<Hospital> example = Example.of(Hospital,matcher);
        Page<Hospital> pages = hospitalRepository.findAll(example, pageable);
        List<Hospital> content = pages.getContent();
        for (Hospital hospital : content) {
            String hostype = hospital.getHostype();
            //根据dicode和value查询医院等级
            String hostype1 = dictFeignClient.getName("Hostype", Integer.parseInt(hostype));
            hospital.getParam().put("hostypeString",hostype1);
            String pravinceCode = dictFeignClient.getName(Integer.parseInt(hospital.getProvinceCode()));
            String cityCode = dictFeignClient.getName(Integer.parseInt(hospital.getCityCode()));
            String districtCode = dictFeignClient.getName(Integer.parseInt(hospital.getDistrictCode()));
            //医院地址
            hospital.getParam().put("fullAddress",pravinceCode+cityCode+districtCode);
        }
        return pages;
    }
}
