package com.eveb.saasops.modules.log.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.log.entity.LogSystem;


@Mapper
public interface LogSystemMapper extends MyMapper<LogSystem> {

}
