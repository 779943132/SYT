package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.model.user.AdminUser;
import com.atguigu.yygh.user.mapper.AdminUserMapper;
import com.atguigu.yygh.user.service.AdminUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser> implements AdminUserService {
    @Override
    public Map<String, Object> login(String username, String password) {
        QueryWrapper<AdminUser> qw = new QueryWrapper<>();
        qw.eq("email",username);
        qw.eq("password",password);
        AdminUser adminUser = baseMapper.selectOne(qw);
        if (adminUser == null) {
            throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
        }
        Map<String,Object> map = new HashMap<>();
        String token = JwtHelper.createToken(adminUser.getId(), adminUser.getName());
        map.put("name",adminUser.getName());
        map.put("email",adminUser.getEmail());
        map.put("roles",adminUser.getRoles());
        map.put("token",token);
        return map;
    }

    @Override
    public Map<String, Object> getInfo(String token) {
        Long userId = JwtHelper.getUserId(token);
        AdminUser adminUser = baseMapper.selectById(userId);

        Map<String,Object> map = new HashMap<>();

        map.put("name",adminUser.getName());

        map.put("email",adminUser.getEmail());

        map.put("roles",adminUser.getRoles());

        map.put("avatar",adminUser.getAvatarUrl());

        return map;
    }

    @Override
    public Boolean updateUserAvatar(String url, String token) {

        if(!StringUtils.isEmpty(token)) {
            Long userId = JwtHelper.getUserId(token);
            AdminUser adminUser = baseMapper.selectById(userId);
            adminUser.setAvatarUrl(url);
            this.updateById(adminUser);
            return true;
        }
        return false;
    }
}
