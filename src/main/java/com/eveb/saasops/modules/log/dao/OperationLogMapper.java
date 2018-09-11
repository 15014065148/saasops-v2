package com.eveb.saasops.modules.log.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.log.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface OperationLogMapper extends MyMapper<OperationLog> {
	 void insertRecord(OperationLog log);
}
