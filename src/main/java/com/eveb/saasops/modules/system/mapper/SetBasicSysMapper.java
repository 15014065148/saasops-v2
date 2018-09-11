package com.eveb.saasops.modules.system.mapper;


import java.util.List;

import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDeposit;


@Mapper
public interface SetBasicSysMapper{

	List<SysDeposit> querydepositAmount(Byte devSource);
	List<SysDeposit> querySetBasicSysdeposit(@Param(value="groupId") Integer groupId);
	
	SysDeposit setBasicSysdepositOne(@Param(value="id") Integer id,@Param(value="groupId") Integer groupId);

	int insertSysSetting(SysSetting sysSetting);
}
