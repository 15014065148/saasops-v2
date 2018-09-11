package com.eveb.saasops.modules.sys.service;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.sys.dao.SysUserMbrgrouprelationMapper;
import com.eveb.saasops.modules.sys.entity.SysUserMbrgrouprelation;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;


@Service
public class SysUserMbrgrouprelationService {
    @Autowired
    private SysUserMbrgrouprelationMapper sysUserMbrgrouprelationMapper;

    public SysUserMbrgrouprelation queryObject(Integer id) {
        return sysUserMbrgrouprelationMapper.selectByPrimaryKey(id);
    }

    public PageUtils queryListPage(SysUserMbrgrouprelation sysUserMbrgrouprelation, Integer pageNo, Integer pageSize, String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if (!StringUtils.isEmpty(orderBy)) {
            PageHelper.orderBy(orderBy);
        }
        List<SysUserMbrgrouprelation> list = sysUserMbrgrouprelationMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public void save(SysUserMbrgrouprelation sysUserMbrgrouprelation) {
        sysUserMbrgrouprelationMapper.insert(sysUserMbrgrouprelation);
    }

    public void update(SysUserMbrgrouprelation sysUserMbrgrouprelation) {
        sysUserMbrgrouprelationMapper.updateByPrimaryKeySelective(sysUserMbrgrouprelation);
    }

    public void delete(Integer id) {
        sysUserMbrgrouprelationMapper.deleteByPrimaryKey(id);
    }

    public void deleteBatch(Integer[] ids) {
        //sysUserMbrgrouprelationMapper.deleteBatch(ids);
    }

    public void saveList(List<SysUserMbrgrouprelation> sysUserMbrgrouprelations) {
        sysUserMbrgrouprelationMapper.insertList(sysUserMbrgrouprelations);
    }

    public void deleteBatchByUserId(Long userId) {
        sysUserMbrgrouprelationMapper.deleteBatchByUserId(userId);
    }

    public List<SysUserMbrgrouprelation> queryListByUserId(Long userId) {
        SysUserMbrgrouprelation record = new SysUserMbrgrouprelation();
        record.setUserId(userId);
        return sysUserMbrgrouprelationMapper.select(record);
    }

}
