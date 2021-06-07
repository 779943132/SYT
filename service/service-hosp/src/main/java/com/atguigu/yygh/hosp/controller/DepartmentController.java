package com.atguigu.yygh.hosp.controller;


import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.repository.DepartmentRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "排班")
@RestController
@RequestMapping("admin/hosp/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    @Autowired
    private DepartmentRepository departmentRepository;
    //查询科室
    @ApiOperation(value = "根据医院编号查询排班")
    @GetMapping("getDeptList/{hospcode}")
    public Result getDeptList(@PathVariable String hospcode){
        List<DepartmentVo> list = departmentService.getDeptList(hospcode);
        return Result.ok(list);
    }
    @GetMapping("test/{hospcode}")
    public Result test(@PathVariable String hospcode){
        Department department = new Department();
        department.setHoscode(hospcode);
        Example<Department> example = Example.of(department);
        System.out.println(example);
        List<Department> all = departmentRepository.findAll(example);

        return Result.ok(all);
    }
}
