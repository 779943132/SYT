package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface HospitalService {
    void save(Map<String, Object> paramMap);

    Hospital getByHoscode(String hoscode);

    Page<Hospital> selectHospPage(int page, int limit, HospitalQueryVo hospitalQueryVo);

    void updateHospStatus(String id, Integer status);

    Map<String,Object> showHospDetail(String id);

    String getHoscodeName(String hoscode);

    List<Hospital> findByHosName(String hosname);

    Map<String, Object> item(String hoscode);
}
