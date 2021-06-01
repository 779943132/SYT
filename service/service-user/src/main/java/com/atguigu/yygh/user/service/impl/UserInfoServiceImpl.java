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
        //验证码判断
        String rediscode = redisTemplate.opsForValue().get(email);

        if (!Objects.equals(rediscode, code)){
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }


        //绑定邮箱
        UserInfo userInfo = null;
        //根据邮箱查询看数据库是否有该用户
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        qw.eq("email",email);
        //判断是否为第一次登录
        UserInfo userEmail = baseMapper.selectOne(qw);

        if(!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.selectWxInfoOpenId(loginVo.getOpenid());
            System.out.println(userInfo);

            if(null != userInfo) {
                //邮箱已存在
                if (userEmail!=null) {
                    int openid = baseMapper.deleteOpenid(loginVo.getOpenid());
                    System.out.println(openid);
                    userEmail.setNickName(userInfo.getNickName());
                    userEmail.setOpenid(userInfo.getOpenid());
                    this.updateById(userEmail);
                }else {
                    userInfo.setEmail(loginVo.getEmail());
                    this.updateById(userInfo);
                }


            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }



        //为空，第一次，添加用户
        if (userEmail == null) {
            UserInfo data = new UserInfo();
            data.setName("");
            data.setStatus(1);
            data.setEmail(email);
            baseMapper.insert(data);
            userEmail = data;
        }



        //不为空，校验看权限
        if (userEmail.getStatus()==0){
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }


        //返回登录后信息
        //返回登录用户名
        //返回token

        Map<String,Object> map = new HashMap<>();
        String name = userEmail.getName();
        if (StringUtils.isEmpty(name)) {
            name = userEmail.getNickName();
        }
        if (StringUtils.isEmpty(name)){
            name=userEmail.getEmail();
        }
        String token = JwtHelper.createToken(userEmail.getId(), userEmail.getName());
        map.put("name",name);
        map.put("token",token);
        return map;

    }

    @Override
    public UserInfo selectWxInfoOpenId(String openId) {
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        qw.eq("openid",openId);
        return  baseMapper.selectOne(qw);
    }
}
