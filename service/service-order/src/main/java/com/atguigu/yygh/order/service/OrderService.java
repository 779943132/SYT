package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface OrderService extends IService<OrderInfo> {
    /**
     * 根据排班id和挂号人id生成订单
     * @param scheduleId
     * @param patientId
     * @return
     */
    Long saveOrder(String scheduleId, Long patientId);

    /**
     * 根据订单id查询订单
     * @param orderId 订单id
     * @return
     */
    OrderInfo getOrder(String orderId);

    /**
     * 查询订单列表带分页
     * @param pages 分页
     * @param orderQueryVo 条件
     * @return
     */
    IPage<OrderInfo> getOrderList(Page<OrderInfo> pages, OrderQueryVo orderQueryVo);

    void delete(String orderId);

    Map<String, Object> show(String orderId);

    Boolean cancelOrder(String orderId);

    /**
     * 就诊通知
     */
    void patientTips();

    //预约统计
    Map<String,Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
