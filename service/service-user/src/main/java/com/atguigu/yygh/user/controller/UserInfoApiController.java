package com.atguigu.yygh.user.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.user.mycode.MyConde;
import com.atguigu.yygh.user.mycode.MailCode;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Api(tags = "用户登录")
@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {
    @Autowired
    private UserInfoService userInfoService;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private MailCode mailCode;
    //用户手机号登录
    @PostMapping("login")
    public Result lohin(@RequestBody LoginVo loginVo){
        Map<String,Object> info =userInfoService.loginUser(loginVo);
        return Result.ok(info);
    }
    @GetMapping("sendCode/{email}")
    public Result sendCode(@PathVariable String email){
        //从redis获取验证码，如果能获取返回ok
        String code = redisTemplate.opsForValue().get(email);
        if (!StringUtils.isEmpty(code)){
            return Result.ok();
        }
        //得不到生成邮件发送，设置有效时间
        // 判断验证码是否一致
        MyConde emailcode = new MyConde();
        //发送验证码
        code = emailcode.generateVerCode();
        Boolean aBoolean = mailCode.sendCode(email, code);
        if (aBoolean){
            redisTemplate.opsForValue().set(email,code,5, TimeUnit.MINUTES);
            return Result.ok();
        }
        return Result.fail().message("发送验证码失败");
    }
}
