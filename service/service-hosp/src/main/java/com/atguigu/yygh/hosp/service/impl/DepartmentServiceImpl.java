package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public List<DepartmentVo> getDeptList(String hospcode) {
        //最终数据封装
        List<DepartmentVo> result =new ArrayList<>();
        Department department = new Department();
        //设置查询值
        department.setHoscode(hospcode);
        Example<Department> ex = Example.of(department);
        List<Department> all = departmentRepository.findAll(ex);
        //根据大科室编号分组
        Map<String, List<Department>> deptmap = all.stream().collect(Collectors.groupingBy(Department::getBigcode));
        for (Map.Entry<String, List<Department>> entry :deptmap.entrySet()){
            //大科室编号
            String bigcode = entry.getKey();
            //大科室下的小科室数据
            List<Department> value = entry.getValue();
            //封装大科室
            DepartmentVo departmentBig = new DepartmentVo();
            departmentBig.setDepcode(bigcode);
            departmentBig.setDepname(value.get(0).getBigname());
            //封装小科室
            List<DepartmentVo> children = new ArrayList<>();
            for (Department department1 : value){
                DepartmentVo departmentMin = new DepartmentVo();
                //将小科室编号放入
                departmentMin.setDepcode(department1.getDepcode());
                //将小科室名字放入
                departmentMin.setDepname(department1.getDepname());
                //将封装后的放入list集合
                children.add(departmentMin);
            }
            //将小科室放入大科室
            departmentBig.setChildren(children);
            //放到最终result
            result.add(departmentBig);
        }
        return result;
    }

    @Override
    public String getDepName(String hoscode, String depcode) {
        Department departmentByHoscodeAndDepcode = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (departmentByHoscodeAndDepcode != null) {
            return departmentByHoscodeAndDepcode.getDepname();
        }
        return null;
    }
}
