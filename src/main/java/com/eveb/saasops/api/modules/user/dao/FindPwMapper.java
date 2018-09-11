package com.eveb.saasops.api.modules.user.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.api.modules.user.entity.FindPwEntity;
import com.eveb.saasops.modules.base.mapper.MyMapper;


@Mapper
public interface FindPwMapper extends MyMapper<FindPwEntity>{

}
