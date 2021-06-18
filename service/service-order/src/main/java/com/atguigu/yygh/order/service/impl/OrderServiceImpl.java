package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.rabbit.constant.MqConst;
import com.atguigu.common.rabbit.service.RabbitService;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.helper.HttpRequestHelper;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hosp.client.HospFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.order.mapper.OrderMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.order.service.WeiXinService;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.EmailVo;
import com.atguigu.yygh.vo.order.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {
    @Autowired
    private PatientFeignClient patientFeignClient;
    @Autowired
    private HospFeignClient hospFeignClient;
    @Autowired
    private RabbitService rabbitService;
    @Autowired
    private WeiXinService weiXinService;
    //生成订单
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //获取就诊人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        //获取排班信息
        ScheduleOrderVo scheduleOrderVo = hospFeignClient.getScheduleOrderVo(scheduleId);

        //判断当前时间是否可以预约
/*        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }*/
        //获取签名信息
        SignInfoVo signInfoVo = hospFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

        //添加到订单表
        OrderInfo orderInfo = new OrderInfo();
        //scheduleOrderVo复制到orderInfo
        BeanUtils.copyProperties(scheduleOrderVo,orderInfo);
        String outTradeNo = System.currentTimeMillis() + ""+ new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientEmail(patient.getEmail());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
        //添加到数据库
        this.save(orderInfo);

        //调用医院接口实现预约挂号操作
        //设置调用医院接口参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("email",patient.getEmail());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        //联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsEmail",patient.getContactsEmail());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);

        //请求医院接口
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, "http://localhost:9998/order/submitOrder");
        System.out.println(result);
        if(result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            //预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            //预约序号
            Integer number = jsonObject.getInteger("number");
            ;
            //取号时间
            String fetchTime = jsonObject.getString("fetchTime");
            ;
            //取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");
            ;
            //更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            //排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            //排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            //发送mq消息，号源更新，邮件通知
            //发送mq进行号源更新
            OrderMqVo orderMqVo = new OrderMqVo();
            //排班编号
            orderMqVo.setScheduleId(scheduleId);
            //排班可预约数
            orderMqVo.setReservedNumber(reservedNumber);
            //排班剩余预约数
            orderMqVo.setAvailableNumber(availableNumber);

            //邮件提示
            EmailVo emailVo = new EmailVo();
            emailVo.setEmail(orderInfo.getPatientEmail());
            //设置预约时间
            String reserveDate =
                    new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")
                            + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                //医生信息
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+scheduleOrderVo.getDocname()+"|"+orderInfo.getTitle());
                //医师服务费
                put("amount", orderInfo.getAmount());
                //安排日期
                put("reserveDate", reserveDate);
                //预约人姓名
                put("name", orderInfo.getPatientName());
                //可退号时间
                put("quitTime", new DateTime(orderInfo.getQuitTime()).toString("yyyy-MM-dd HH:mm"));
            }};
            emailVo.setParam(param);

            orderMqVo.setEmailVo(emailVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
        }else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    @Override
    public OrderInfo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        return this.packOrderInfo(orderInfo);
    }

    @Override
    public IPage<OrderInfo> getOrderList(Page<OrderInfo> pages, OrderQueryVo orderQueryVo) {
        String keyword = orderQueryVo.getKeyword();//医院名称
        Long patientId = orderQueryVo.getPatientId();//就诊人id
        String orderStatus = orderQueryVo.getOrderStatus();//订单状态
        String reserveDate = orderQueryVo.getReserveDate();//安排时间
        String createTimeBegin = orderQueryVo.getCreateTimeBegin();//开始时间
        String createTimeEnd = orderQueryVo.getCreateTimeEnd();//结束时间
        String patientName = orderQueryVo.getPatientName();
        QueryWrapper<OrderInfo> qw = new QueryWrapper<>();
        if (!StringUtils.isEmpty(keyword)) {
             qw.like("hosname",keyword);
        }
        if (!StringUtils.isEmpty(patientId)) {
            qw.eq("patient_id",patientId);
        }
        if (!StringUtils.isEmpty(patientName)) {
            qw.eq("patient_name",patientName);
        }
        if (!StringUtils.isEmpty(reserveDate)) {
            qw.ge("reserve_date",reserveDate);
        }
        if (!StringUtils.isEmpty(orderStatus)) {
            qw.eq("order_status",orderStatus);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            qw.ge("create_time",createTimeBegin);
        }
        if (!StringUtils.isEmpty(keyword)) {
            qw.le("create_time",createTimeEnd);
        }
        IPage<OrderInfo> orderInfoPage = baseMapper.selectPage(pages, qw);
        //状态码封装
        orderInfoPage.getRecords().forEach(this::packOrderInfo);
        return orderInfoPage;
    }

    @Override
    public void delete(String orderId) {
        baseMapper.deleteById(orderId);
    }

    @Override
    public Map<String, Object> show(String orderId) {
        Map<String, Object> map = new HashMap<>();
        OrderInfo orderInfo = this.packOrderInfo(this.getById(orderId));
        map.put("orderInfo", orderInfo);
        Patient patient
                =  patientFeignClient.getPatientOrder(orderInfo.getPatientId());
        map.put("patient", patient);
        return map;
    }

    @Override
    public Boolean cancelOrder(String orderId) {
        //得到订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        //判断是否可以取消
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        //过了取消订单的时间
/*        if (quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }*/
        //调用医院接口实现取消
        SignInfoVo signInfoVo = hospFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updateCancelStatus");
        if (result.getInteger("code")!=200){
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }else {
            System.out.println(1);
            //判断当前订单是否要退款
            if(orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                Boolean isRefund = weiXinService.refund(orderId);
                if (!isRefund){
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
                return this.runMq(orderInfo);
            }
            System.out.println(2);
            //未支付，不退款，直接修改号数
            return this.runMq(orderInfo);
        }
    }

    /**
     * 处理状态码
     * @param orderInfo
     * @return
     */
    private OrderInfo packOrderInfo(OrderInfo orderInfo){
        orderInfo.getParam().put("orderStatusString",OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }

    private Boolean runMq(OrderInfo orderInfo){
        System.out.println(3);
        if (orderInfo != null) {
            //设置订单状态为取消预约
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            System.out.println("订单号"+orderInfo);
            //更新
            baseMapper.updateById(orderInfo);

            OrderMqVo orderMqVo = new OrderMqVo();
            //向mq传入科室id
            orderMqVo.setScheduleId(orderInfo.getScheduleId());
            //邮件提示
            EmailVo msmVo = new EmailVo();

            msmVo.setTemplateCode("退号");
            msmVo.setEmail(orderInfo.getPatientEmail());
            String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
            Map<String,Object> param = new HashMap<String,Object>(){{
                put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
                put("reserveDate", reserveDate);
                put("name", orderInfo.getPatientName());
            }};
            //设置邮件信息
            msmVo.setParam(param);
            orderMqVo.setEmailVo(msmVo);
            rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER, MqConst.ROUTING_ORDER, orderMqVo);
            return true;
        }
       return false;
    }

    @Override
    public void patientTips() {

        //根据安排时间和状态进行查询
        QueryWrapper<OrderInfo> qw =new QueryWrapper<>();
        //今天
        qw.eq("reserve_date",new DateTime().toString("yyyy-MM-dd"));
        //状态不等于-1
        qw.ne("order_status",OrderStatusEnum.CANCLE.getStatus());
        List<OrderInfo> orderInfos = baseMapper.selectList(qw);
        System.out.println(orderInfos);
        for (OrderInfo orderInfo : orderInfos) {
            this.EmailRemind(orderInfo);
        }
    }
    public void EmailRemind(OrderInfo orderInfo){
        //邮件提示
        EmailVo msmVo = new EmailVo();
        msmVo.setTemplateCode("提醒");
        msmVo.setEmail(orderInfo.getPatientEmail());
        String reserveDate = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd") + (orderInfo.getReserveTime()==0 ? "上午": "下午");
        Map<String,Object> param = new HashMap<String,Object>(){{
            put("title", orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle());
            put("reserveDate", reserveDate);
            put("name", orderInfo.getPatientName());
        }};
        //设置邮件信息
        System.out.println(param);
        msmVo.setParam(param);
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_MSM, MqConst.ROUTING_MSM_ITEM,msmVo);
    }

    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        List<OrderCountVo> orderCountList = baseMapper.selectCount(orderCountQueryVo);
        //获取日期
        List<String> dateList = orderCountList.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        //获取数量
        List<Integer> countList = orderCountList.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        Map<String,Object> map = new HashMap<>();
        map.put("dateList",dateList);
        map.put("countList",countList);
        return map;
    }
}
