package com.atguigu.yygh.cmn.controller;

import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.model.cmn.Dict;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Api(tags= "数据字典管理")
@RestController
@RequestMapping("/admin/cmn/dict")
@CrossOrigin
public class DictController {
    @Autowired
    private DictService dictService;

    @ApiOperation(value = "根据id查询子数据")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id){
        List<Dict> list =dictService.findChlidData(id);
        return Result.ok(list);
    }

    //导出数据字典
    @ApiOperation(value = "导出数据字典")
    @GetMapping("exportData")
    public void exportDict(HttpServletResponse response){
        dictService.exportDictData(response);
    }
    //导入数据字典
    @ApiOperation(value = "导入数据字典")
    @PostMapping("importExport")
    public void importExportDict(MultipartFile file){
        dictService.importExport(file);
    }

    //根据dictcode和valu值查询
    @ApiOperation("根据dictCode和value查询等级")
    @GetMapping("getName/{dictCode}/{value}")
    public String getName(@PathVariable String dictCode ,@PathVariable int value){
        return dictService.getDictName(dictCode,value);
    }
    //根据vaue查询
    @ApiOperation("根据value查询等级")
    @GetMapping("getName/{value}")
    public String getName(@PathVariable int value){
        return dictService.getDictName("",value);
    }
    //根据dictCode查询下级节点
    @ApiOperation("根据dictCode查询下级节点")
    @GetMapping("findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode){
       List<Dict> list= dictService.findByDictCode(dictCode);
       return Result.ok(list);
    }

}
