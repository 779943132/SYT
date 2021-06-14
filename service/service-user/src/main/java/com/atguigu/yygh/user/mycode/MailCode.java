package com.atguigu.yygh.user.mycode;

import com.atguigu.yygh.vo.msm.EmailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
        Map<String, Object> param = emailVo.getParam();
        param.get("title");
        String text= "尊敬的用户"+param.get("name")+
                    ",您好:\n您预约的"+ param.get("title") +
                    "已成功,医师服务费为"+param.get("amount")+
                    "安排日期为"+param.get("reserveDate")+
                    "请凭身份证完成取号操作如需退号请在"+param.get("quitTime")+
                    "之前完成。(这是一封自动发送的邮件，请不要直接回复）";
        return myEmailUtils.myEmail("779943132@qq.com", emailVo.getEmail(), "预约挂号订单", text);
    }
}
