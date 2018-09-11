package com.eveb.saasops.modules.system.domain.dao;

import com.eveb.saasops.modules.system.domain.entity.SystemDomain;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;


@Mapper
@Component
public interface SystemDomainMapper extends MyMapper<SystemDomain> {

}
