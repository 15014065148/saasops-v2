package com.eveb.saasops.api.modules.user.mapper;


import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.api.modules.user.entity.FindPwEntity;

@Mapper
public interface ApiUserMapper {

	//FindPwEntity findPwdOne(@Param("loginName") String loginName);
	int insertFindPwd(FindPwEntity entity);
}
