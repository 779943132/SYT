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

import java.util.Map;

@RestController
@Api(tags = "医院信息")
@RequestMapping("admin/hosp/hospital")
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
    @ApiOperation(value = "更新上线装态")
    @GetMapping("updateHospStatus/{id}/{status}")
    public Result updateHospStatus(@PathVariable String id,@PathVariable Integer status){
        hospitalService.updateHospStatus(id,status);
        return Result.ok();
    }
    @ApiOperation(value = "医院详情信息")
    @GetMapping("showHospDetail/{id}")
    public Result showHospDetail(@PathVariable String id){
        Map<String,Object> hospital = hospitalService.showHospDetail(id);
        return Result.ok(hospital);
    }


}
