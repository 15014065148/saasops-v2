package com.eveb.saasops.modules.member.dao;

import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.member.entity.MbrAccount;

import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.IdsMapper;

@Component
@Mapper
public interface MbrAccountMapper extends MyMapper<MbrAccount>{

}
