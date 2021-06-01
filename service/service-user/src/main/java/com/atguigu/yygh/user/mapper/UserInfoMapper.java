package com.atguigu.yygh.user.mapper;

import com.atguigu.yygh.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {
    int deleteOpenid(@Param("openid") String openid);
}
