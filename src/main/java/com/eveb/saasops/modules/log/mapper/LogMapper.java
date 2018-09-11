package com.eveb.saasops.modules.log.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eveb.saasops.modules.log.entity.LogMbrLogin;

@Mapper
public interface LogMapper {
	LogMbrLogin findMemberLoginLastOne(String loginName);
	
	int deleteMbrLoginBatch(@Param("idArr") String[] idArr);
	
	int deleteMbrRegBatch(@Param("idArr") String[] idArr);
	
	int deleteSystemBatch(@Param("idArr") String[] idArr);
	
	int updateLoginTime(@Param("id") Integer id);
	
}
