package com.eveb.saasops.modules.sys.dao;

import com.eveb.saasops.modules.sys.entity.SysUserAgyaccountrelation;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface SysUserAgyaccountrelationMapper extends MyMapper<SysUserAgyaccountrelation> {

	public void deleteBatchByUserId(Long userId);
}
