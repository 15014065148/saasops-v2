package com.eveb.saasops.modules.system.systemsetting.dao;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.eveb.saasops.modules.system.systemsetting.dto.StationSet;

@Component
@Mapper
public interface StationSetMapper {
	
	/**
	 * 查询会员获取数据默认天数
	 * @return
	 */
	StationSet queryConfigDaysAndScope();
	
}
