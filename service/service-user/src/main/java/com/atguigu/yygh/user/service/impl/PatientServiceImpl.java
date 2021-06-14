package com.atguigu.yygh.user.service.impl;

import com.atguigu.yygh.cmn.client.DictFeignClient;
import com.atguigu.yygh.enums.DictEnum;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.user.mapper.PatientMapper;
import com.atguigu.yygh.user.service.PatientService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    @Autowired
    private DictFeignClient dictFeignClient;

    @Override
    public List<Patient> findAllById(Long userId) {
        //根据userid查就诊人
        List<Patient> patientlist = baseMapper.selectList(new QueryWrapper<Patient>().eq("user_id", userId));
        //根据数据字典，得到编码对应的内容
        //对属性进行封装
        patientlist.forEach(item->{
             this.packPatient(item);
        });
        return patientlist;
    }

    //根据id获取就诊人
    @Override
    public Patient getPatientId(Long id) {
        return this.packPatient(baseMapper.selectById(id));
    }

    private Patient packPatient(Patient patient) {
        //根据证件编码获取证件类型具体值
        String CertificatesType = dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(), Integer.parseInt(patient.getCertificatesType()));
        //联系人证件类型
        String contactsCertificatesTypeString =
                dictFeignClient.getName(DictEnum.CERTIFICATES_TYPE.getDictCode(),Integer.parseInt(patient.getContactsCertificatesType()));
        //省
        String provinceString = dictFeignClient.getName(Integer.parseInt(patient.getProvinceCode()));
        //市
        String cityString = dictFeignClient.getName(Integer.parseInt(patient.getCityCode()));
        //区
        String districtString = dictFeignClient.getName(Integer.parseInt(patient.getDistrictCode()));
        patient.getParam().put("certificatesTypeString", CertificatesType);
        patient.getParam().put("contactsCertificatesTypeString", contactsCertificatesTypeString);
        patient.getParam().put("provinceString", provinceString);
        patient.getParam().put("cityString", cityString);
        patient.getParam().put("districtString", districtString);
        patient.getParam().put("fullAddress", provinceString + cityString + districtString + patient.getAddress());
        return patient;
    }
}
