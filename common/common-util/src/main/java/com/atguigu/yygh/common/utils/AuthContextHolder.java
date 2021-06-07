package com.atguigu.yygh.common.utils;

import com.atguigu.yygh.common.helper.JwtHelper;

import javax.servlet.http.HttpServletRequest;

public class AuthContextHolder {
    //得到用户id
    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        return JwtHelper.getUserId(token);
    }
    //得到用户名称
    public static String getUserName(HttpServletRequest request){
        String token = request.getHeader("token");
        return JwtHelper.getUserName(token);
    }
}
