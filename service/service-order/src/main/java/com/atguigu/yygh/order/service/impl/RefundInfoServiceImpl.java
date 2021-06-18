package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.RefundStatusEnum;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.model.order.RefundInfo;
import com.atguigu.yygh.order.mapper.RefundInfoMapper;
import com.atguigu.yygh.order.service.RefundInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class RefundInfoServiceImpl extends ServiceImpl<RefundInfoMapper,RefundInfo> implements RefundInfoService {
    @Override
    public RefundInfo saveRefundInfo(PaymentInfo paymentInfo) {
        RefundInfo refundInfo = baseMapper.selectOne(new QueryWrapper<RefundInfo>().eq("order_id", paymentInfo.getOrderId()).eq("payment_type", paymentInfo.getPaymentType()));
        //有相同数据
        if (refundInfo != null) {
            return refundInfo;
        }
        //添加记录
        refundInfo = new RefundInfo();
        refundInfo.setCreateTime(new Date());//退款时间
        refundInfo.setOrderId(paymentInfo.getOrderId());//订单id
        refundInfo.setPaymentType(paymentInfo.getPaymentType());//付款方式
        refundInfo.setOutTradeNo(paymentInfo.getOutTradeNo());//订单编号
        refundInfo.setRefundStatus(RefundStatusEnum.UNREFUND.getStatus());//状态
        refundInfo.setSubject(paymentInfo.getSubject());
        //paymentInfo.setSubject("test");
        refundInfo.setTotalAmount(paymentInfo.getTotalAmount());
        baseMapper.insert(refundInfo);
        return refundInfo;
    }
}
