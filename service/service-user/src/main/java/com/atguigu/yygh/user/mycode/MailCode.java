package com.atguigu.yygh.user.mycode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MailCode {
    @Autowired
    private MyEmailUtils myEmailUtils;

    public Boolean sendCode(String email,String code){

        String text= "尊敬的用户,您好:\n本次请求的邮件验证码为 "+ code+ " ,本验证码5分钟内有效，请及时输入。（请勿泄露此验证码如非本人操作，请忽略该邮件。\n(这是一封自动发送的邮件，请不要直接回复）";
        Boolean ok = myEmailUtils.myEmail("779943132@qq.com", email, "预约挂号验证码", text);
        return ok;
    }
}
