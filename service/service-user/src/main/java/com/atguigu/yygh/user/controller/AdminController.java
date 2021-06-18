package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.user.service.AdminUserService;
import com.atguigu.yygh.vo.acl.AvatarVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Api(tags = "管理员操作")
@RestController
@RequestMapping("/admin/user")
public class AdminController {
    @Autowired
    private AdminUserService adminUserService;

    @ApiOperation(value = "登录")
    @PostMapping("/login/{username}/{password}")
    public Result login( @PathVariable String username,@PathVariable String password){
        Map<String,Object> info = adminUserService.login(username,password);
        return Result.ok(info);
    }
    @ApiOperation(value = "获取用户信息")
    @GetMapping("/getInfo/{token}")
    public Result getInfo(@PathVariable String token){
        Map<String,Object> info = adminUserService.getInfo(token);
        return Result.ok(info);
    }

    @ApiOperation(value = "更新用户头像")
    @PostMapping("/updateUserAvatar")
    public Result updateUserAvatar(@RequestBody AvatarVo avatarVo){
        Boolean ok = adminUserService.updateUserAvatar(avatarVo.getUrl(),avatarVo.getToken());
        return Result.ok(ok);
    }
}
