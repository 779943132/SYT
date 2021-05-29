package com.atguigu.yygh.user.mycode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
@Component
public class MyEmailUtils {
    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 普通邮件发送
     * @param from 发送者
     * @param to 接受人
     * @param subject 邮件主题
     * @param content 内容
     */
    public Boolean myEmail(String from,String to,String subject,String content){
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件发送人
        message.setFrom(from);
        //邮件接收人
        message.setTo(to);
        //邮件主题
        message.setSubject(subject);
        //邮件内容
        message.setText(content);
        //发送邮件
        try {
            javaMailSender.send(message);
            return true;
        }catch (Exception e){
            return false;
        }

    }

    /**
     *
     * @param from 发件人
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @param html 是否开启html支持
     */
    public void myEmailHtml(String from,String to,String subject,String content,Boolean html){
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            //发件人
            helper.setFrom(from);
            //收件人
            helper.setTo(to);
            //主题
            helper.setSubject(subject);
            //内容，是否开启html支持
            helper.setText(content,html);
            //发送
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("发送失败");
        }
    }
}
