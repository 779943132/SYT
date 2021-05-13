package com.atguigu.yygh.hosp.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface HospitalService {
    void save(Map<String, Object> paramMap);
}
