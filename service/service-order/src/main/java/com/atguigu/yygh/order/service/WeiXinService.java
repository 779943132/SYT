package com.atguigu.yygh.order.service;

import java.util.Map;

public interface WeiXinService {
    Map<String, Object> createNative(String orderId);

    Map<String, String> queryPayStatus(String orderId);

    /***
     * 退款
     * @param orderId
     * @return
     */
    Boolean refund(String orderId);
}
