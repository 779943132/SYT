package com.atguigu.yygh.user.mycode;

import com.atguigu.yygh.vo.msm.EmailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class MailCode {
    @Autowired
    private MyEmailUtils myEmailUtils;

    public Boolean sendCode(String email,String code){

        String text= "尊敬的用户,您好:\n本次请求的邮件验证码为 "+ code+ " ,本验证码5分钟内有效，请及时输入。（请勿泄露此验证码如非本人操作，请忽略该邮件。\n(这是一封自动发送的邮件，请不要直接回复）";
        return myEmailUtils.myEmail("779943132@qq.com", email, "预约挂号验证码", text);
    }
    public boolean send(EmailVo emailVo) {
        System.out.println(emailVo);
        //判断是退号，还是挂号
        if (!StringUtils.isEmpty(emailVo.getTemplateCode())&&emailVo.getTemplateCode().equals("退号")){

            Map<String, Object> param = emailVo.getParam();
            String text= "尊敬的用户"+param.get("name")+
                    ",您好:\n您预约的"+ param.get("title") +
                    "安排日期为"+param.get("reserveDate")+
                    "的号已完成退号，欢迎下次预约。(这是一封自动发送的邮件，请不要直接回复）";
            return myEmailUtils.myEmail("779943132@qq.com", emailVo.getEmail(), "预约挂号平台取消预约通知", text);
        }else if (!StringUtils.isEmpty(emailVo.getTemplateCode()) && emailVo.getTemplateCode().equals("提醒")){
            Map<String, Object> param = emailVo.getParam();
            String text= "尊敬的用户"+param.get("name")+
                    ",您好:\n您预约的"+ param.get("title") +
                    "安排日期为"+param.get("reserveDate")+
                    "请在规定时间去医院进行取号就医，过期作废。(这是一封自动发送的邮件，请不要直接回复）";
            return myEmailUtils.myEmail("779943132@qq.com", emailVo.getEmail(), "预约挂号平台取消预约通知", text);
        }else {
            Map<String, Object> param = emailVo.getParam();
            String text= "尊敬的用户"+param.get("name")+
                    ",您好:\n您预约的"+ param.get("title") +
                    "已成功,医师服务费为"+param.get("amount")+
                    "安排日期为"+param.get("reserveDate")+
                    "请凭身份证完成取号操作如需退号请在"+param.get("quitTime")+
                    "之前完成。(这是一封自动发送的邮件，请不要直接回复）";
            return myEmailUtils.myEmail("779943132@qq.com", emailVo.getEmail(), "预约挂号平台预约成功通知", text);
        }
    }
}
