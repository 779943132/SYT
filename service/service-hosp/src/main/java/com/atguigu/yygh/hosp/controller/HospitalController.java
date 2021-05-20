package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "医院信息")
@RequestMapping("admin/hosp/hospital")
@CrossOrigin
public class HospitalController {
    @Autowired
    private HospitalService hospitalService;
    //医院列表
    @ApiOperation(value = "获取医院列表")
    @GetMapping("list/{page}/{limit}")
    public Result listHosp(@PathVariable int page, @PathVariable int limit,HospitalQueryVo hospitalQueryVo){
        Page<Hospital> pagelist = hospitalService.selectHospPage(page,limit,hospitalQueryVo);
        return Result.ok(pagelist);
    }

}
