package com.eveb.saasops.modules.system.cmpydeposit.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.system.cmpydeposit.entity.SysDeposit;

@Mapper
public interface SysDepositMapper extends MyMapper<SysDeposit> {

	public List<SysDeposit> querySysDepositList(SysDeposit sysDeposit);
}
