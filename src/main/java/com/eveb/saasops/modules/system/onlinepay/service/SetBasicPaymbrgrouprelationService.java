package com.eveb.saasops.modules.system.onlinepay.service;

import org.springframework.util.StringUtils;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.eveb.saasops.modules.system.onlinepay.dao.SetBasicPaymbrgrouprelationMapper;
import com.eveb.saasops.modules.system.onlinepay.entity.SetBasicPaymbrgrouprelation;


@Service
public class SetBasicPaymbrgrouprelationService {
    @Autowired
    private SetBasicPaymbrgrouprelationMapper setBasicPaymbrgrouprelationMapper;

    public SetBasicPaymbrgrouprelation queryObject(Integer id) {
        return setBasicPaymbrgrouprelationMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(SetBasicPaymbrgrouprelation setBasicPaymbrgrouprelation, Integer pageNo, Integer pageSize,String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if(!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<SetBasicPaymbrgrouprelation> list = setBasicPaymbrgrouprelationMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public void save(SetBasicPaymbrgrouprelation setBasicPaymbrgrouprelation) {
            setBasicPaymbrgrouprelationMapper.insert(setBasicPaymbrgrouprelation);
    }

    public void update(SetBasicPaymbrgrouprelation setBasicPaymbrgrouprelation) {
            setBasicPaymbrgrouprelationMapper.updateByPrimaryKeySelective(setBasicPaymbrgrouprelation);
    }

    public void delete(Integer id) {
            setBasicPaymbrgrouprelationMapper.deleteByPrimaryKey(id);
    }

    public int multiSave(List<SetBasicPaymbrgrouprelation> setBasicPaymbrgrouprelations){
        return setBasicPaymbrgrouprelationMapper.insertList(setBasicPaymbrgrouprelations);
    }
    public void deleteByonlinePayId(SetBasicPaymbrgrouprelation setBasicPaymbrgrouprelation){
        setBasicPaymbrgrouprelationMapper.delete(setBasicPaymbrgrouprelation);
    }
}
