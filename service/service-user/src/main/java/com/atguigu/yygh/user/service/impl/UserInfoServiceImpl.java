package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.mapper.UserInfoMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private PatientService patientService;
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

    //用户认证
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //根据用户id查询信息
        UserInfo userInfo = baseMapper.selectById(userId);
        //设置认证信息
        userInfo.setName(userAuthVo.getName());//姓名
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //更新
        baseMapper.updateById(userInfo);
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        String keyword=null;
        Integer status=null;
        Integer authStatus=null;
        String createTimeBegin=null;
        String createTimeEnd=null;
        if (userInfoQueryVo != null) {
             keyword = userInfoQueryVo.getKeyword();//用户名称
             status = userInfoQueryVo.getStatus();//用户状态
             authStatus= userInfoQueryVo.getAuthStatus();//认证状态
             createTimeBegin= userInfoQueryVo.getCreateTimeBegin();//开始时间
             createTimeEnd = userInfoQueryVo.getCreateTimeEnd();//结束时间
        }
        //得到查询条件

        //对条件进行非空判断
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
            qw.like("name",keyword);
        }
        if (!StringUtils.isEmpty(status)) {
            qw.eq("status",status);
        }
        if (!StringUtils.isEmpty(keyword)) {
            qw.eq("auth_status",authStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            qw.ge("create_time",createTimeBegin);
        }
        if (!StringUtils.isEmpty(keyword)) {
            qw.le("create_time",createTimeEnd);
        }
        Page<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, qw);
        List<UserInfo> records = userInfoPage.getRecords();
        records.forEach(this::packageUserInfo);
        return userInfoPage;
    }

    @Override
    public void lock(Long id, Integer status) {
        if (status == 0||status==1) {
            UserInfo userInfo = baseMapper.selectById(id);
            userInfo.setStatus(status);
            updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> show(Long id) {
        Map<String,Object> map = new HashMap<>();
        UserInfo userInfo = this.packageUserInfo(baseMapper.selectById(id));
        map.put("userInfo",userInfo);
        //查询就诊人信息
        List<Patient> allById = patientService.findAllById(id);
        map.put("patientList",allById);
        return map;
    }

    //代号处理
    private UserInfo packageUserInfo(UserInfo userInfo) {
        //处理认证
        userInfo.getParam().put("authStatusString",AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        //处理状态
        userInfo.getParam().put("Status",userInfo.getStatus()==0 ? "锁定" : "正常");
        return userInfo;
    }

    @Override
    public UserInfo selectWxInfoOpenId(String openId) {
        QueryWrapper<UserInfo> qw = new QueryWrapper<>();
        qw.eq("openid",openId);
        return  baseMapper.selectOne(qw);
    }
}
