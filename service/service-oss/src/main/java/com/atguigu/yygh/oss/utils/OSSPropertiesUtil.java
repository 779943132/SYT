package com.atguigu.yygh.oss.utils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OSSPropertiesUtil implements InitializingBean {


    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKey}")
    private String accessKey;

    @Value("${aliyun.oss.secret}")
    private String secret;

    @Value("${aliyun.oss.bucket}")
    private String bucket;

    public static String ALIYUN_OSS_ENDPOINT;
    public static String ALIYUN_OSS_ACCESSKEY;
    public static String ALIYUN_OSS_SECRET;
    public static String ALIYUN_OSS_BUCKET;


    @Override
    public void afterPropertiesSet() throws Exception {
        ALIYUN_OSS_ENDPOINT = endpoint;
        ALIYUN_OSS_ACCESSKEY = accessKey;
        ALIYUN_OSS_SECRET = secret;
        ALIYUN_OSS_BUCKET=bucket;
    }
}
