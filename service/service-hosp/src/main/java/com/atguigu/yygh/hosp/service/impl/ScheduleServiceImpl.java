package com.atguigu.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.yygh.hosp.repository.ScheduleRepository;
import com.atguigu.yygh.hosp.service.ScheduleService;
import com.atguigu.yygh.model.hosp.Department;
import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.DepartmentQueryVo;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ScheduleServiceImpl implements ScheduleService {
    @Autowired
    private ScheduleRepository scheduleRepository;
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
}
