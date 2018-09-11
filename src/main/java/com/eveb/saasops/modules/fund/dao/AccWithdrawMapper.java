package com.eveb.saasops.modules.fund.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.fund.entity.AccWithdraw;
import org.apache.ibatis.annotations.Mapper;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface AccWithdrawMapper extends MyMapper<AccWithdraw>,IdsMapper<AccWithdraw>{

}
