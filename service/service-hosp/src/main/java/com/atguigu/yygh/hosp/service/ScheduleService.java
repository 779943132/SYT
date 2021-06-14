package com.atguigu.yygh.hosp.service;

import com.atguigu.yygh.model.hosp.Schedule;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.hosp.ScheduleQueryVo;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface  ScheduleService extends IService<Schedule> {
    void save(Map<String, Object> paramMap);

    Page<Schedule> getScheduleList(ScheduleQueryVo dqv, int page, int limit);

    void remove(String hoscode,String hosscheduleid);

    Map<String, Object> getScheduleRule(int page, int limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Object getBookingSchedule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleId(String scheduleId);

    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    //更新排班数据
    void update(Schedule schedule);
}
