package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.vo.hosp.DepartmentVo;
import com.baomidou.mybatisplus.extension.api.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@Api(tags = "排班")
@RestController
@RequestMapping("admin/hosp/department")
public class DepartmentController {
    @Autowired
    private DepartmentService departmentService;
    //查询科室
    @ApiOperation(value = "根据医院编号查询排班")
    @GetMapping("getDeptList/{hospcode}")
    public Result getDeptList(@PathVariable String hospcode){
        List<DepartmentVo> list = departmentService.getDeptList(hospcode);
        return Result.ok(list);
    }
}
