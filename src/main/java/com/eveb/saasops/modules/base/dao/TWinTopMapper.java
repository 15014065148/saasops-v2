package com.eveb.saasops.modules.base.dao;
import org.apache.ibatis.annotations.Mapper;

import com.eveb.saasops.modules.base.entity.TWinTop;
import com.eveb.saasops.modules.base.mapper.MyMapper;

import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface TWinTopMapper extends MyMapper<TWinTop>,IdsMapper<TWinTop> {

}
