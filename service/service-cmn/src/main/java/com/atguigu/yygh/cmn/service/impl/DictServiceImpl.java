package com.atguigu.yygh.cmn.service.impl;

import com.atguigu.yygh.cmn.mapper.DictMapper;
import com.atguigu.yygh.cmn.service.DictService;
import com.atguigu.yygh.model.cmn.Dict;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
//继承ServiceImpl就注入mapper了
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    @Override
    //获取id下面的子数据
    public List<Dict> findChlidData(Long id) {
        QueryWrapper<Dict> qw = new QueryWrapper();
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
    //判断id下面是否有字节点
    private boolean isChlidren(Long id){
        QueryWrapper<Dict> qw = new QueryWrapper();
        qw.eq("parent_id",id);
        //得到查询数据的数量
        Integer integer = baseMapper.selectCount(qw);
        //如果integer>0下面成立就返回true,如果integer<=0,下面不成立返回false
        return integer>0;
    }
}
