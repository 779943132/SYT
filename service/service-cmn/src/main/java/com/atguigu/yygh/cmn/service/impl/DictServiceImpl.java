package com.atguigu.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.atguigu.yygh.cmn.listener.DictListener;
import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.atguigu.yygh.vo.cmn.DictEeVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//继承ServiceImpl就注入mapper了
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Autowired
    private DictListener dictListener;

    @Override
    //获取id下面的子数据
    @Cacheable(value = "dict",keyGenerator = "keyGenerator")//key的命名
    public List<Dict> findChlidData(Long id) {
        QueryWrapper<Dict> qw = new QueryWrapper<>();
        qw.eq("parent_id",id);
        List<Dict> dicts = baseMapper.selectList(qw);
        //设置setHasChildren的值
        for (Dict dict : dicts) {
            //判断每个Dist是否有子数据
            boolean chlidren = isChlidren(dict.getId());
            //给HasChildren设置值
            dict.setHasChildren(chlidren);
        }
        return dicts;
    }

    @Override
    //导出数据字典
    @CacheEvict(value = "dict",allEntries = true)//将dict中内容清空
    public void exportDictData(HttpServletResponse response) {
        try {
            //设置下载信息
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
            //查询信息，条件为null表示查询所有
            List<Dict> dictList = baseMapper.selectList(null);
            List<DictEeVo> dictVoList = new ArrayList<>(dictList.size());
            for(Dict dict : dictList) {
                DictEeVo dictVo = new DictEeVo();
                //将dict内容复制到DictVo中
                BeanUtils.copyProperties(dict, dictVo);
                dictVoList.add(dictVo);
            }
            //向export中写入信息
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("数据字典").doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void importExport(MultipartFile file) {
        try {
            //将export中数据上传到数据库
            EasyExcel.read(file.getInputStream(),DictEeVo.class,dictListener).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //判断id下面是否有字节点
    private boolean isChlidren(Long id){
        QueryWrapper<Dict> qw = new QueryWrapper<>();
        qw.eq("parent_id",id);
        //得到查询数据的数量
        Integer integer = baseMapper.selectCount(qw);
        //如果integer>0下面成立就返回true,如果integer<=0,下面不成立返回false
        return integer>0;
    }

    @Override
    public String getDictName(String dictCode, int value) {
        if (StringUtils.isEmpty(dictCode)) {
            QueryWrapper<Dict> qw = new QueryWrapper<>();
            qw.eq("value",value);
            Dict dict = baseMapper.selectOne(qw);
            return dict.getName();
        }else {
            QueryWrapper<Dict> qw = new QueryWrapper<>();
            //根据dictcode查询
            qw.eq("dict_code",dictCode);
            Dict dict1 = baseMapper.selectOne(qw);
            //得到查出来的id,并作为第二次查询的条件
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("parent_id",dict1.getId()).eq("value",value));
            return dict.getName();
        }
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        QueryWrapper<Dict> qw = new QueryWrapper<>();
        qw.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(qw);
        return baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id",dict.getId()));
    }
}
