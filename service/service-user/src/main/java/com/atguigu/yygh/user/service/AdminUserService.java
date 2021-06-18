package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.AdminUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface AdminUserService extends IService<AdminUser> {
    Map<String, Object> login(String username, String password);

    Map<String, Object> getInfo(String token);

    Boolean updateUserAvatar(String url, String token);
}
