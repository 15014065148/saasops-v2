package com.eveb.saasops.api.modules.apisys.dao;
import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.api.modules.apisys.entity.TCpSite;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface TCpSiteMapper extends MyMapper<TCpSite> {



}