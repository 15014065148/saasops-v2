package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.operate.entity.SetGmGame;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;

@Mapper
public interface SetGmGameMapper extends MyMapper<SetGmGame>, IdsMapper<SetGmGame> {

}
