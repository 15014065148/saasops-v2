package com.eveb.saasops.modules.base.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.entity.BaseFinancialCode;
import com.eveb.saasops.modules.base.mapper.MyMapper;

@Mapper
public interface BaseFinancialCodeMapper extends MyMapper<BaseFinancialCode>{
	
}