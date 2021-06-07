package com.atguigu.yygh.user.api;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mycode.MyConde;
import com.atguigu.yygh.user.mycode.MailCode;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    @ApiOperation(value = "用户登录")
    @PostMapping("login")
    public Result lohin(@RequestBody LoginVo loginVo){
        Map<String,Object> info =userInfoService.loginUser(loginVo);
        return Result.ok(info);
    }

    @ApiOperation(value = "获取邮箱验证码")
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
        code = MyConde.generateVerCode();
        Boolean aBoolean = mailCode.sendCode(email, code);
        if (aBoolean){
            redisTemplate.opsForValue().set(email,code,5, TimeUnit.MINUTES);
            return Result.ok();
        }
        return Result.fail().message("发送验证码失败");
    }

    //用户认证
    @ApiOperation(value = "用户认证")
    @PostMapping("auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){
        //第一个参数，用户id,第二个vo对象
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return Result.ok();
    }
    //获取用户认证信息
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = AuthContextHolder.getUserId(request);
        UserInfo byId = userInfoService.getById(userId);
        return Result.ok(byId);
    }
}
