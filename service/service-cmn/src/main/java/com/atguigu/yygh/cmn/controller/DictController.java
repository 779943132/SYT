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
}
