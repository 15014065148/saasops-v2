package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.operate.entity.SetGmGame;
import com.eveb.saasops.modules.operate.entity.TGameLogo;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.IdsMapper;

@Mapper
public interface TGameLogoMapper extends MyMapper<TGameLogo>, IdsMapper<SetGmGame> {

}
