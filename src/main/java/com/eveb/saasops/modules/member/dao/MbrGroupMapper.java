package com.eveb.saasops.modules.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.member.entity.MbrGroup;


@Mapper
public interface MbrGroupMapper extends MyMapper<MbrGroup> {

}
