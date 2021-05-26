package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface DepartmentService {
    void save(Map<String, Object> paramMap);

    Page<Department> getDepartmentList(DepartmentQueryVo dqv, int page, int limit);

    void remove(String hoscode,String depcode);

    List<DepartmentVo> getDeptList(String hospcode);

    String getDepName(String hoscode, String depcode);
}
