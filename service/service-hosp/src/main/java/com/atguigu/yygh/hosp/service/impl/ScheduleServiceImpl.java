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
        //??????pageable page???0??????
        Pageable pageable = PageRequest.of(page-1,limit);
        //??????Example??????,??????????????????????????????
        ExampleMatcher matcher = ExampleMatcher.matching().withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING).withIgnoreCase(true);
        Schedule schedule = new Schedule();
        //???vo??????????????????????????????
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
        //??????Template??????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        //????????????????????????
        Aggregation agg= Aggregation.newAggregation(
                Aggregation.match(criteria),//????????????
                Aggregation.group("workDate")//????????????
                .first("workDate").as("workDate")
                //????????????
                .count().as("docCount")
                .sum("reservedNumber").as("reservedNumber")
                .sum("availableNumber").as("availableNumber"),
                //??????
                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                //??????
                Aggregation.skip((page-1)*limit),
                Aggregation.limit(limit)
        );


        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();
        //????????????????????????
        Aggregation totalAgg= Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalAggregate = mongoTemplate.aggregate(totalAgg, Schedule.class, BookingScheduleRuleVo.class);

        int size = totalAggregate.getMappedResults().size();//??????????????????
        //???????????????????????????
        for (BookingScheduleRuleVo bookingScheduleRuleVo : mappedResults) {
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }
        //??????????????????
        Map<String,Object> map =new HashMap<>();
        map.put("bookingScheduleRuleList",mappedResults);
        map.put("total",size);
        //??????????????????
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
    //??????????????????????????? ????????????????????????????????????????????????
    private Schedule packageSchedule(Schedule schedule) {
        if (schedule != null) {
            schedule.getParam().put("hosname",hospitalService.getHoscodeName(schedule.getHoscode()));
            schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
            schedule.getParam().put("dayofWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
            return schedule;
        }
        return null;
    }

    //????????????????????????
    @Override
    public Object getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String,Object> data = new HashMap<>();
        //??????????????????
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();

        //??????????????????????????????
        IPage iPage =  this.getListDate(page,limit,bookingRule);
        List<Date> dateList = iPage.getRecords();
        //???????????????????????????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate").first("workDate").as("workDate")
                .count().as("docCount").sum("availableNumber").as("availableNumber")
                .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregateResult =
                mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        //????????????????????????
        List<BookingScheduleRuleVo> scheduleResults = aggregateResult.getMappedResults();

        //?????????????????????????????????????????????????????????????????????
        Map<Date,BookingScheduleRuleVo> scheduleRuleVoMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(scheduleResults)){
            scheduleRuleVoMap = scheduleResults.stream().collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,BookingScheduleRuleVo->BookingScheduleRuleVo));
        }
        //???????????????????????????
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i=0,len=dateList.size();i<len;i++) {
            Date date = dateList.get(i);

            //????????????????????????

            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleRuleVoMap.get(date);

            //????????????????????????
            if (bookingScheduleRuleVo == null) {
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                //??????????????????
                bookingScheduleRuleVo.setDocCount(0);
                //???????????????-1??????
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date);
            bookingScheduleRuleVo.setWorkDateMd(date);
            //????????????????????????????????????
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);

            //????????????????????????????????????????????? ?????? 0?????? 1???????????? -1??????????????????
            if (i == len-1 && page == iPage.getPages()){
                bookingScheduleRuleVo.setStatus(1);
            }else {
                bookingScheduleRuleVo.setStatus(0);
            }
            //?????????????????????????????????????????????
            if (i==0 && page ==1){
                DateTime stopTime = this.getDateTime(new Date(),bookingRule.getStopTime());
                if (stopTime.isBeforeNow()){
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }

            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }
        //????????????
        //??????????????????
        data.put("bookingScheduleList",bookingScheduleRuleVoList);
        //??????
        data.put("total",iPage.getTotal());
        //????????????
        Map<String,String> baseMap = new HashMap<>();
        //????????????
        baseMap.put("hosname",hospitalService.getHoscodeName(hoscode));
        //??????
        Department department = departmentService.getDepartment(hoscode,depcode);
        //???????????????
        baseMap.put("bigname",department.getBigname());
        //???????????????
        baseMap.put("depname",department.getDepname());
        //???
        baseMap.put("workDateString",new DateTime().toString("yyyy???MM???"));
        //????????????
        baseMap.put("releaseTime",bookingRule.getReleaseTime());
        //????????????
        baseMap.put("stopTime",bookingRule.getStopTime());
        data.put("baseMap",baseMap);
        return data;
    }


    //?????????????????????
    private IPage getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        //??????????????????????????? ??? ??? ???
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //??????????????????
        Integer cycle = bookingRule.getCycle();
        //???????????????????????????????????????????????????????????????+1
        if(releaseTime.isBeforeNow()){
            cycle+=1;
        }
        //??????????????????????????????????????????????????????
        List<Date> dateList = new ArrayList<>();
        for (int i =0;i<cycle;i++){
            //???????????????
            DateTime curDataTime = new DateTime().plusDays(i);
            //??????????????????
            String dateString = curDataTime.toString("yyyy-MM-dd");
            //???????????????list
            dateList.add(new DateTime(dateString).toDate());
        }
        //?????????????????????????????????7????????????7?????????
        List<Date> pagetDataList = new ArrayList<>();
        int start = (page-1)*limit;
        int end = (page-1)*limit+limit;
        //????????????????????????7????????????
        if (end > dateList.size()){
            end=dateList.size();
        }
        for (int i=start;i<end;i++){
            pagetDataList.add(dateList.get(i));
        }
        //??????7??????
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page,7,dateList.size());
        return iPage.setRecords(pagetDataList);
    }

    //????????????id??????????????????
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
        //????????????????????????
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if (bookingRule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        scheduleOrderVo.setHoscode(schedule.getHoscode());//??????
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

        //?????????????????????????????????????????????-1????????????0???
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //??????????????????
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //??????????????????
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //????????????????????????
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    //???????????????????????????mq
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    /**
     * ???Date?????????yyyy-MM-dd HH:mm????????????DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
    }
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = "";
        switch (dateTime.getDayOfWeek()) {
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "??????";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "??????";
            default:
                break;
        }
        return dayOfWeek;
    }

}
