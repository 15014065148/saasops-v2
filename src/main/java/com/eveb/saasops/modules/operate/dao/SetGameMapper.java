package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.operate.entity.SetGame;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface SetGameMapper extends MyMapper<SetGame>,IdsMapper<SetGame> {

}
