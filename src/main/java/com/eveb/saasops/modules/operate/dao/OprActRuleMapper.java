package com.eveb.saasops.modules.operate.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.operate.entity.OprActRule;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.IdsMapper;

@Mapper
public interface OprActRuleMapper extends MyMapper<OprActRule>, IdsMapper<OprActRule> {

}
