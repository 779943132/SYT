package com.atguigu.yygh.user.receive;

import com.atguigu.common.rabbit.constant.MqConst;
import com.atguigu.yygh.user.mycode.MailCode;
import com.atguigu.yygh.vo.msm.EmailVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SmsReceiver {
    @Autowired
    private MailCode mailCode;
    /*
     *MQ邮件发送
     * */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MqConst.QUEUE_MSM_ITEM, durable = "true"),
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_MSM),
            key = {MqConst.ROUTING_MSM_ITEM}
    ))
    public void send(EmailVo emailVo, Message message, Channel channel) {
        mailCode.send(emailVo);
    }
}
