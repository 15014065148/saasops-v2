package com.eveb.saasops.modules.agent.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.agent.entity.Commission;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommissionMapper extends MyMapper<Commission> {

}