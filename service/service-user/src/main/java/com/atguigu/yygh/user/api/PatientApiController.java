package com.atguigu.yygh.user.api;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.service.PatientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "就诊人管理")
@RestController
@RequestMapping("/api/user/patient")
public class PatientApiController {

    @Autowired
    private PatientService patientService;


    //获取就诊人
    @ApiOperation(value = "获取就诊人")
    @GetMapping("auth/findAll")
    public Result findAll(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        System.out.println(userId);
        List<Patient> list =  patientService.findAllById(userId);
        return Result.ok(list);
    }
    //添加就诊人
    @ApiOperation(value = "添加就诊人")
    @PostMapping("auth/save")
    public Result savePatient(@RequestBody Patient patient,HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        patientService.save(patient);
        return Result.ok();
    }
    //根据id获取就诊人信息
    @ApiOperation(value = "获取就诊人信息")
    @GetMapping("auth/get/{id}")
    public Result getPatient(@PathVariable String id){
        Patient patient = patientService.getPatientId(id);
        return Result.ok(patient);
    }

    //修改就诊人
    @ApiOperation(value = "获取就诊人信息")
    @PostMapping("auth/update")
    public Result updatePatient(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }
    //删除就诊人
    @ApiOperation(value = "删除就诊人信息")
    @DeleteMapping("auth/delect/{id}")
    public Result delectPatient(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }

}
