package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.hosp.client.HospFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private HospFeignClient hospFeignClient;
    /**
     * 向支付记录表添加数据
     * @param order
     * @param status
     */
    @Override
    public void savePaymentInfo(OrderInfo order, Integer status) {
        QueryWrapper<PaymentInfo> qw = new QueryWrapper();
        qw.eq("order_id",order.getId());
        qw.eq("payment_type",status);
        if (baseMapper.selectCount(qw)>0){
            return;
        }
        //添加记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(status);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"+order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);
    }

    /**
     * 更新订单状态
     * @param out_trade_no 订单编号
     * @param resultMap 微信返回值
     */
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        //根据订单号，得到支付记录
        PaymentInfo paymentInfo = baseMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("out_trade_no", out_trade_no).eq("payment_type", PaymentTypeEnum.WEIXIN.getStatus()));
        //更新支付信息
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());//设置以支付
        paymentInfo.setTradeNo(resultMap.get("transaction_id"));//业务编号
        paymentInfo.setCallbackTime(new Date());//支付时间
        paymentInfo.setCallbackContent(resultMap.toString());//微信回调的所有数据
        this.updateById(paymentInfo);//更新
        //根据订单号，得到订单信息//更新订单信息
        OrderInfo order = orderService.getOrder(paymentInfo.getOrderId().toString());
        order.setOrderStatus(OrderStatusEnum.PAID.getStatus());//设置已支付
        orderService.updateById(order);//更新

        //调用医院接口，更新支付订单信息
        SignInfoVo signInfoVo = hospFeignClient.getSignInfoVo(order.getHoscode());//得到相关对象
        Map<String,Object> map = new HashMap<>();
        map.put("hoscode",order.getHoscode());
        map.put("hosRecordId",order.getHosRecordId());
        map.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(map, signInfoVo.getSignKey());
        map.put("sign", sign);
        JSONObject jsonObject = HttpRequestHelper.sendRequest(map, signInfoVo.getApiUrl() + "/order/updatePayStatus");
    }

    @Override
    public PaymentInfo getPaymentInfo(String orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }
}
