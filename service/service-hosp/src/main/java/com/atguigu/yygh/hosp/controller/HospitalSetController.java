package com.atguigu.yygh.hosp.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.MD5;
import com.atguigu.yygh.model.hosp.HospitalSet;
import com.atguigu.yygh.hosp.service.HospitalSetService;
import com.atguigu.yygh.vo.hosp.HospitalSetQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Random;

@RestController
@Api(tags = "医院设置管理")
@RequestMapping("/admin/hosp/hospitalSet")
@CrossOrigin //允许跨域访问
public class HospitalSetController {
    @Autowired
    private HospitalSetService hospitalSetService;
    //查询医院设置表所有信息
    //http://127.0.0.1:8201/admin/hosp/hospitalSet/filndAll
    @ApiOperation(value = "获取所有医院设置信息")
    @GetMapping("filndAll")
    public Result findAllHospitalSet(){
        //调用service方法
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }
    //逻辑删除医院设置
    @ApiOperation(value = "根据id逻辑删除医院设置")
    @DeleteMapping("hospitalSet/{id}")
    public Result removeHospSet(@PathVariable Long id){
        boolean b = hospitalSetService.removeById(id);
        if (b){
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    //3.条件查询带分页
    //@RequestBody(required = false) 通过json传数据，required = false表示可以为空
    @ApiOperation(value = "条件查询带分页")
    @PostMapping("findPage/{curent}/{limit}")
    public Result findPageHospitalSet(@PathVariable long curent,
                                      @PathVariable long limit,
                                      @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){
        //创建page对象
        Page<HospitalSet> page =new Page<>(curent,limit);
        //构造条件
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        //判断是否有值，有值进行模糊查询
        if(!StringUtils.isEmpty(hospitalSetQueryVo.getHosname())){
            //模糊查询，通过hospitalSetQueryVo获取医院名称
            wrapper.like("hosname",hospitalSetQueryVo.getHosname());
        }
        if (!StringUtils.isEmpty(hospitalSetQueryVo.getHoscode())){
           //模糊查询，通过hospitalSetQueryVo获取医院id
           wrapper.eq("hoscode",hospitalSetQueryVo.getHoscode());
        }

        Page<HospitalSet> page1 = hospitalSetService.page(page, wrapper);
        return Result.ok(page1);
    }
    //4.添加医院设置接口
    @ApiOperation(value = "添加医院设置接口")
    @PostMapping("saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        //设置状态
        hospitalSet.setStatus(1);
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis()+""+random.nextInt(1000)));
        boolean save = hospitalSetService.save(hospitalSet);
        if (save) {
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    //5.根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("getHospSet/{id}")
    public Result getHospSet(@PathVariable Long id){
        HospitalSet byId = hospitalSetService.getById(id);
        return Result.ok(byId);
    }
    //6.修改医院设置接口
    @ApiOperation(value = "修改医院设置接口")
    @PostMapping("updateHospitalSet")
    public Result updateHospitalSet(@RequestBody HospitalSet hospitalSet){
        boolean b = hospitalSetService.updateById(hospitalSet);
        if (b) {
            return Result.ok();
        }else {
            return Result.fail();
        }
    }
    //7.批量删除医院设置接口
    @ApiOperation(value = "批量删除医院设置接口")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> list){
        hospitalSetService.removeByIds(list);
        return Result.ok();
    }
    //8.医院设置锁定和解锁
    @PutMapping("lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,@PathVariable Integer status){
        //根据id查询医院设置信息
        HospitalSet byId = hospitalSetService.getById(id);
        //设置状态
        byId.setStatus(status);
        //调用方法
        hospitalSetService.updateById(byId);
        return Result.ok();
    }
    //9.发送签名密钥
    @GetMapping("sendKey/{id}")
    public Result  sendKey(@PathVariable Long id){
        HospitalSet byId = hospitalSetService.getById(id);
        //获取密钥
        byId.getSignKey();
        //获取医院编号
        byId.getHoscode();
        //获取医院名字
        byId.getHosname();
        return Result.ok();
    }
}
