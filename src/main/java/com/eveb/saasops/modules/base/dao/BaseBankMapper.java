package com.eveb.saasops.modules.base.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.entity.BaseBank;
import com.eveb.saasops.modules.base.mapper.MyMapper;
@Mapper
public interface BaseBankMapper extends MyMapper<BaseBank>{
	
}