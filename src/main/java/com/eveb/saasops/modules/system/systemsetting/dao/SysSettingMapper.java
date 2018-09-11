package com.eveb.saasops.modules.system.systemsetting.dao;

import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.system.systemsetting.entity.SysSetting;


@Mapper
public interface SysSettingMapper extends MyMapper<SysSetting> {
	
}
