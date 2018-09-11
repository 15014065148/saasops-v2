package com.eveb.saasops.modules.log.service;

import com.eveb.saasops.common.utils.BeanUtil;
import com.eveb.saasops.common.utils.PageUtils;
import com.eveb.saasops.modules.base.service.BaseService;
import com.eveb.saasops.modules.log.dao.OperationLogMapper;
import com.eveb.saasops.modules.log.entity.OperationLog;

import org.springframework.util.StringUtils;
import com.github.pagehelper.PageHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;




@Service
public class OperationLogService extends BaseService<OperationLogMapper, OperationLog>{

    @Autowired
    private OperationLogMapper operationLogMapper;

    public PageUtils queryListPage(OperationLog operationLog, Integer pageNo, Integer pageSize,String orderBy) {
        PageHelper.startPage(pageNo, pageSize);
        if(!StringUtils.isEmpty(orderBy))
        	PageHelper.orderBy(orderBy);
        List<OperationLog> list = operationLogMapper.selectAll();
        return BeanUtil.toPagedResult(list);
    }

    public void insert(OperationLog operationLog) {
    	operationLogMapper.insert(operationLog);
    }
    
}
