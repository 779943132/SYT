package com.atguigu.yygh.order.controller;

import com.atguigu.yygh.common.result.Result;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeiXinService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Api(tags = "微信支付")
@RestController
@RequestMapping("/api/order/weixin")
public class WxController {
    @Autowired
    private WeiXinService weiXinService;
    @Autowired
    private PaymentService paymentService;
    //生成微信支付扫描
    @ApiOperation("生成微信二维码")
    @GetMapping("createNative/{orderId}")
    public Result createNative(@PathVariable String orderId){
        //生成微信二维码
        Map<String,Object> map = weiXinService.createNative(orderId);

        return Result.ok(map);
    }
    //生成微信支付扫描
    @ApiOperation("查询支付状态")
    @GetMapping("queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable String orderId){
        //调用微信接口查询支付状态
        Map<String,String> resultMap = weiXinService.queryPayStatus(orderId);
        //支付判断
        if(resultMap == null){
            return Result.fail().message("支付出错了");
        }
        if("SUCCESS".equals(resultMap.get("trade_state"))){
            //更新订单状态
            String out_trade_no = resultMap.get("out_trade_no");
            paymentService.paySuccess(out_trade_no,resultMap);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
