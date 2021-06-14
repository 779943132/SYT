package com.atguigu.yygh.user.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.JwtHelper;
import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.utils.ConstantPropertiesUtil;
import com.atguigu.yygh.user.utils.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static org.reflections.Reflections.log;

@Controller
@RequestMapping("/api/ucenter/wx")
public class WeixinApiController {
    @Autowired
    private UserInfoService userInfoService;

    //1生成微信二维码
    //返回生成二维码参数
    @GetMapping("getLoginParam")
    @ResponseBody
    public Result genQrConnect() {

        try {
            Map<String, Object> map = new HashMap<>();
            map.put("appid", ConstantPropertiesUtil.WX_OPEN_APP_ID);
            map.put("scope", "snsapi_login");
            //map.put("scope", "snsapi_userinfo");
            String wxOpenRedirectUrl = URLEncoder.encode(ConstantPropertiesUtil.WX_OPEN_REDIRECT_URL, "utf-8");
            map.put("redirect_uri", wxOpenRedirectUrl);
            map.put("state", System.currentTimeMillis() + "");
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    //1 回调的方法
    @GetMapping("callback")
    public String callback(String code, String state) {
        //获取临时票据
        //拿code请求微信固定地址获得两个值
        if (StringUtils.isEmpty(state) || StringUtils.isEmpty(code)) {
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }

        //使用code和appid以及appscrect换取access_token
        //%s为占位符
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        //填充占位符
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantPropertiesUtil.WX_OPEN_APP_ID,
                ConstantPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        //使用工具类向微信发送请求
        try {
            String accessTokenInfo= HttpClientUtils.get(accessTokenUrl);
            //将得到的信息变成json对象
            JSONObject jsonObject = JSONObject.parseObject(accessTokenInfo);
            //得到access_token和openid
            String access_token = jsonObject.getString("access_token");
            String openId = jsonObject.getString("openid");

            //判断数据库是否存在扫码人
            UserInfo userInfoExist = userInfoService.selectWxInfoOpenId(openId);

            if (userInfoExist==null){
            //拿着两个参数再请求一个地址，得到扫码人信息
            String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                    "?access_token=%s" +
                    "&openid=%s";
            String userInfoUrl = String.format(baseUserInfoUrl, access_token, openId);
            String resultInfo = HttpClientUtils.get(userInfoUrl);
            System.out.println("resultInfo"+resultInfo);
            //将得到的信息变成json对象
            JSONObject resultObject = JSONObject.parseObject(resultInfo);
            //得到昵称
            String nickname = resultObject.getString("nickname");
            //得到头像
            String headimgurl = resultObject.getString("headimgurl");

            //将扫码人信息添加到数据库
            UserInfo userInfo = new UserInfo();
            //昵称
            userInfo.setNickName(nickname);
            //是否锁定
            userInfo.setStatus(1);
            userInfo.setOpenid(openId);
            //将数据加入数据库
            userInfoService.save(userInfo);
            userInfoExist  = userInfo;
        }
            //返回name和token字符串
            Map<String,String> map = new HashMap<>();
            //得到用户名称
            String name = userInfoExist.getName();
            //如果名称为空，将名字设置为昵称
            if (StringUtils.isEmpty(name)) {
                name = userInfoExist.getNickName();
            }
            //如果昵称也为空，将名字设置为邮箱
            if (StringUtils.isEmpty(name)){
                name = userInfoExist.getEmail();
            }
            //将名字放入map
            map.put("name",name);
            //判断userInfo中是否有邮箱
            //后期判断是否需要绑定邮箱，如果openid为空不需要绑定邮箱，不为空，绑定
            if (StringUtils.isEmpty(userInfoExist.getEmail())){
                map.put("openid",userInfoExist.getOpenid());
            }else {
                map.put("openid","");
            }
            //使用JWT生成token字符串
            String token = JwtHelper.createToken(userInfoExist.getId(),name);
            map.put("token",token);
            //使用重定向到前端
            return "redirect:" + ConstantPropertiesUtil.YYGH_BASE_URL + "/weixin/callback?token="+map.get("token")+"&openid="+map.get("openid")+"&name="+URLEncoder.encode(map.get("name"),"utf-8");

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

