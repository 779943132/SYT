package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.order.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
