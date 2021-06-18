package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.common.utils.AuthContextHolder;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Api(tags = "订单管理")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {
    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "生成挂号订单")
    @PostMapping("auth/submitOrder/{scheduleId}/{patientId}")
    public Result saveOrder(@PathVariable Long patientId, @PathVariable String scheduleId){
        Long orderLong = orderService.saveOrder(scheduleId,patientId);
        return Result.ok(orderLong);
    }

    @ApiOperation(value = "根据订单id查询订单详情信息")
    @GetMapping("auth/getOrders/{orderId}")
    public Result getOrder(@PathVariable String orderId){
        OrderInfo order = orderService.getOrder(orderId);
        return Result.ok(order);
    }

    @ApiOperation(value = "查询订单列表")
    @GetMapping("auth/getOrderList/{page}/{limit}")
    public Result getOrderList(@PathVariable Long limit, @PathVariable Long page, OrderQueryVo orderQueryVo, HttpServletRequest request){
        orderQueryVo.setUserId(AuthContextHolder.getUserId(request));
        Page<OrderInfo> pages = new Page<>(page,limit);
        IPage<OrderInfo> orderList = orderService.getOrderList(pages, orderQueryVo);
        return Result.ok(orderList);
    }

    @ApiOperation(value = "查询所有订单状态")
    @GetMapping("auth/getStatusList")
    public Result getStatusList(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "取消预约")
    @GetMapping("auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable String orderId){
        Boolean isOrder = orderService.cancelOrder(orderId);
        return Result.ok(isOrder);
    }
}
