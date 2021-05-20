package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.repository.query.ExampleQueryMapper;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    //上传科室信息
    @Override
    public void save(Map<String, Object> paramMap) {
        String data = JSONObject.toJSONString(paramMap);
        Department department = JSONObject.parseObject(data,Department.class);
        //查询的时候可以根据要查询的条件编写方法，这样用Repository会自动生成方法
        Department dp =departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(),department.getDepcode());
        if (dp!=null){
            dp.setUpdateTime(new Date());
            dp.setIsDeleted(0);
            departmentRepository.save(dp);
        }else {
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }

    }
    //查询科室信息
    @Override
    public Page<Department> getDepartmentList(DepartmentQueryVo dqv, int page, int limit) {
        //创建pageable page从0开始
        Pageable pageable = PageRequest.of(page-1,limit);
        //创建Example对象,模糊查询，忽略大小写
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Department department = new Department();
        //将vo中数据传到正常对象中
        BeanUtils.copyProperties(dqv,department);
        department.setIsDeleted(0);
        Example<Department> example = Example.of(department,matcher);
        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode,String depcode) {
        Department departmentByDepcode = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);
        if (departmentByDepcode!=null){
            departmentRepository.delete(departmentByDepcode);
        }
    }
}
