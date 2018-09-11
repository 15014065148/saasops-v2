package com.eveb.saasops.modules.system.agencydomain.dao;

import com.eveb.saasops.modules.system.agencydomain.entity.SystemAgencyUrl;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
@Component
public interface SystemAgencyUrlMapper extends MyMapper<SystemAgencyUrl>,IdsMapper<SystemAgencyUrl>{

}
