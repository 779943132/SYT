package com.atguigu.yygh.oss.service.Impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.atguigu.yygh.oss.service.FileService;
import com.atguigu.yygh.oss.utils.OSSPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Override
    public String upload(MultipartFile file) {
        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = OSSPropertiesUtil.ALIYUN_OSS_ENDPOINT;
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = OSSPropertiesUtil.ALIYUN_OSS_ACCESSKEY;
        String accessKeySecret = OSSPropertiesUtil.ALIYUN_OSS_SECRET;
        String bucket = OSSPropertiesUtil.ALIYUN_OSS_BUCKET;

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //生成UUID使文件名称不重复
        String uuid = UUID.randomUUID().toString().replaceAll("-","");
        //根据日期生成文件夹
        String timeUrl = new DateTime().toString("yyyy/MM/dd");
        String fileName = timeUrl+"/"+uuid + file.getOriginalFilename();
        // 调用方法实现上传
        ossClient.putObject(bucket, fileName, inputStream);

        // 关闭OSSClient。
        ossClient.shutdown();
        //上传后路径
        return "https://"+bucket+"."+endpoint+"/"+fileName;
    }
}
