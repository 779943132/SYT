package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.common.exception.YyghException;
import com.atguigu.yygh.common.result.ResultCodeEnum;
import com.atguigu.yygh.hosp.mapper.ScheduleMapper;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.DepartmentService;
import com.atguigu.yygh.hosp.service.HospitalService;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.BookingRule;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Hospital;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.BookingScheduleRuleVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper,Schedule> implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private HospitalService hospitalService;
    @Autowired
    private DepartmentService departmentService;
    @Override
    public void save(Map<String, Object> paramMap) {

        String ScheduleString = JSONObject.toJSONString(paramMap);
        Schedule schedule = JSONObject.parseObject(ScheduleString, Schedule.class);
        Schedule selectSchedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if (selectSchedule != null) {
            selectSchedule.setUpdateTime(new Date());
            selectSchedule.setIsDeleted(0);
            selectSchedule.setStatus(1);
            scheduleRepository.save(selectSchedule);
        }else {
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }

    }

    @Override
    public Page<Schedule> getScheduleList(ScheduleQueryVo dqv, int page, int limit) {
        //创建pageable page从0开始
        Pageable pageable = PageRequest.of(page-1,limit);
        //创建Example对象,模糊查询，忽略大小写
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Schedule schedule = new Schedule();
        //将vo中数据传到正常对象中
        BeanUtils.copyProperties(dqv,schedule);
        schedule.setIsDeleted(0);
        Example<Schedule> example = Example.of(schedule,matcher);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    @Override
    public void remove(String hoscode, String hosscheduleid) {

        //Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode,hosscheduleid);
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        schedule.setHosScheduleId(hosscheduleid);
        Example<Schedule> example = Example.of(schedule);
        List<Schedule> scheduledata = scheduleRepository.findAll(example);
        if (scheduledata.get(0)!=null){
            scheduleRepository.delete(scheduledata.get(0));
        }
    }

    @Override
    public Map<String, Object> getScheduleRule(int page, int limit, String hoscode, String depcode) {
        //使用Template查询
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //根据日期进行分组
        Aggregation agg= Aggregation.newAggregation(
                Aggregation.match(criteria),//匹配条件
                Aggregation.group("workDate")//分组字段
                .first("workDate").as("workDate")
                //号源统计
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //排序
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //分页
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );


        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //分组查询总记录数
        Aggregation totalAgg= Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggregate = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        int size = totalAggregate.getMappedResults().size();//得到总记录数
        //把日期对应星期获取
        for (BookingScheduleRuleVo bookingScheduleRuleVo : mappedResults) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //设置最终数据
        Map<String,Object> map =new HashMap<>();
        map.put("bookingScheduleRuleList",mappedResults);
        map.put("total",size);
        //获取医院名称
        String name = hospitalService.getHoscodeName(hoscode);
        Map<String,String> bashMap = new HashMap<>();
        bashMap.put("hosname",name);

        map.put("baseMap",bashMap);

        return map;

    }

    @Override
    public List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> scheduleslist = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDateTime());
        for (Schedule schedule : scheduleslist) {
            this.packageSchedule(schedule);
        }
        return scheduleslist;
    }
    //封装排班详情其他值 医院名称，科室名称，日期对应星期
    private Schedule packageSchedule(Schedule schedule) {
        if (schedule != null) {
            schedule.getParam().put("hosname",hospitalService.getHoscodeName(schedule.getHoscode()));
            schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
            schedule.getParam().put("dayofWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
            return schedule;
        }
        return null;
    }

    //获取可排班的数据
    @Override
    public Object getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String,Object> data = new HashMap<>();
        //获取预约规则
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //获取可预约的日期数据
        IPage iPage =  this.getListDate(page,limit,bookingRule);
        List<Date> dateList = iPage.getRecords();
        //获取可预约日期的科室剩余数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                .count().as("docCount").sum("availableNumber").as("availableNumber")
                .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregateResult =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        //获取可预约的数量
        List<BookingScheduleRuleVo> scheduleResults = aggregateResult.getMappedResults();

        //对数据进行封装，使日期对应可预约数，和预约规则
        Map<Date,BookingScheduleRuleVo> scheduleRuleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleResults)){
            scheduleRuleVoMap = scheduleResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,BookingScheduleRuleVo->BookingScheduleRuleVo));
        }
        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i=0,len=dateList.size();i<len;i++) {
            Date date = dateList.get(i);

            //根据日期得到规则

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleRuleVoMap.get(date);

            //当天没有排班医生
            if (bookingScheduleRuleVo == null) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //就诊医生人数
                bookingScheduleRuleVo.setDocCount(0);
                //科室无号用-1表示
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //计算当前预约日期对应星期
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录即将预约， 状态 0正常 1即将放号 -1当天停止挂号
            if (i == len-1 && page == iPage.getPages()){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //如果过了当天预约时间，不能预约
            if (i==0 && page ==1){
                DateTime stopTime = this.getDateTime(new Date(),bookingRule.getStopTime());
                if (stopTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //数据封装
        //预约规则数据
        data.put("bookingScheduleList",bookingScheduleRuleVoList);
        //总数
        data.put("total",iPage.getTotal());
        //其他数据
        Map<String,String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname",hospitalService.getHoscodeName(hoscode));
        //科室
        Department department = departmentService.getDepartment(hoscode,depcode);
        //大科室名称
        baseMap.put("bigname",department.getBigname());
        //小科室名称
        baseMap.put("depname",department.getDepname());
        //月
        baseMap.put("workDateString",new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime",bookingRule.getReleaseTime());
        //停号时间
        baseMap.put("stopTime",bookingRule.getStopTime());
        data.put("baseMap",baseMap);
        return data;
    }


    //获取可预约数据
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //获取当天的放号时间 年 月 日
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //获取预约周期
        Integer cycle = bookingRule.getCycle();
        //当天放号时间过去，预约周期从第二天算，周期+1
        if(releaseTime.isBeforeNow()){
            cycle+=1;
        }
        //获取可预约所有日期，最后以统即将放号
        List<Date> dateList = new ArrayList<>();
        for (int i =0;i<cycle;i++){
            //后面的天数
            DateTime curDataTime = new DateTime().plusDays(i);
            //将日期格式化
            String dateString = curDataTime.toString("yyyy-MM-dd");
            //将日期存入list
            dateList.add(new DateTime(dateString).toDate());
        }
        //预约周期不同，每页显示7天，超过7天分页
        List<Date> pagetDataList = new ArrayList<>();
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        //如果显示数据小与7直接显示
        if (end > dateList.size()){
            end=dateList.size();
        }
        for (int i=start;i<end;i++){
            pagetDataList.add(dateList.get(i));
        }
        //大于7分页
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,7,dateList.size());
        return iPage.setRecords(pagetDataList);
    }

    //根据排班id获取排班信息
    @Override
    public Schedule getScheduleId(String scheduleId) {
        if (scheduleId != null) {
            return this.packageSchedule(scheduleRepository.findScheduleById(scheduleId));
        }
        return null;
    }

    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        Schedule schedule = scheduleRepository.findScheduleById(scheduleId);
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if (bookingRule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        scheduleOrderVo.setHoscode(schedule.getHoscode());//编号
        scheduleOrderVo.setHosname(hospitalService.getHoscodeName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setDocname(schedule.getDocname());
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    //更新排班信息，用于mq
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }
        return dayOfWeek;
    }

}
