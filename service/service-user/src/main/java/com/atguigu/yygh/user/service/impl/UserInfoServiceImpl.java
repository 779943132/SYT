package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper,UserInfo> implements UserInfoService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Override
    public Map<String, Object> loginUser(LoginVo loginVo) {
        //得到信息
        String email = loginVo.getEmail();
        String code = loginVo.getCode();
        //判断是否为空
        if (StringUtils.isEmpty(email)||StringUtils.isEmpty(code)) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        /*// 判断验证码是否一致
        MyConde emailcode = new MyConde();
        //发送验证码
        String s = emailcode.generateVerCode();
        Date fromDate = new Date();
        String text= "尊敬的用户,您好:\n本次请求的邮件验证码为 "+ s+ " ,本验证码5分钟内有效，请及时输入。（请勿泄露此验证码如非本人操作，请忽略该邮件。\n(这是一封自动发送的邮件，请不要直接回复）";
        myEmailUtils.myEmail("779943132@qq.com",email,"预约挂号验证码",text);
        //code过期时间为1分钟，过期抛出异常*/
        String rediscode = redisTemplate.opsForValue().get(email);

        if (!Objects.equals(rediscode, code)){
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        //判断是否为第一次登录
        UserInfo userinfo = baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("email", email));
        //为空，第一次，添加用户
        if (userinfo == null) {
            UserInfo data = new UserInfo();
            data.setName(email);
            data.setStatus(1);
            data.setEmail(email);
            baseMapper.insert(data);
            userinfo = data;
        }
        //不为空，校验看权限
        if (userinfo.getStatus()==0){
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //返回登录后信息
        //返回登录用户名
        //返回token

        Map<String,Object> map = new HashMap<>();
        String name = userinfo.getName();
        if (StringUtils.isEmpty(name)) {
            name = userinfo.getNickName();
        }
        if (StringUtils.isEmpty(name)){
            name=userinfo.getEmail();
        }
        String token = JwtHelper.createToken(userinfo.getId(), userinfo.getName());
        map.put("name",name);
        map.put("token",token);
        return map;
    }
}
