package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.vo.order.OrderCountQueryVo;
import com.atguigu.yygh.vo.order.OrderQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/order/orderInfo")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @ApiOperation(value = "查询订单列表")
    @GetMapping("getOrderList/{page}/{limit}")
    public Result getOrderList(@PathVariable Long limit, @PathVariable Long page, OrderQueryVo orderQueryVo){
        Page<OrderInfo> pages = new Page<>(page,limit);
        IPage<OrderInfo> orderList = orderService.getOrderList(pages, orderQueryVo);
        return Result.ok(orderList);
    }
    @ApiOperation(value = "根据订单id查询订单详情信息")
    @GetMapping("getOrders/{orderId}")
    public Result getOrder(@PathVariable String orderId){
        Map<String,Object> order = orderService.show(orderId);
        System.out.println(order.get("patient"));
        return Result.ok(order);
    }
    @ApiOperation(value = "查询所有订单状态")
    @GetMapping("getStatusList")
    public Result getStatusList(){
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    @ApiOperation(value = "根据订单id删除订单详情信息")
    @DeleteMapping("deleteOrders/{orderId}")
    public Result deleteOrder(@PathVariable String orderId){
        orderService.delete(orderId);
        return Result.ok();
    }

    @ApiOperation(value = "预约数量统计")
    @DeleteMapping("getStatisticsMap")
    public Result getCountMap(OrderCountQueryVo orderCountQueryVo){
        System.out.println(orderCountQueryVo);
        return Result.ok(orderService.getCountMap(orderCountQueryVo));
    }
}
