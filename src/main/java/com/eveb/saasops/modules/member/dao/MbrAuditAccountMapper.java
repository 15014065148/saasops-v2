package com.eveb.saasops.modules.member.dao;

import com.eveb.saasops.modules.fund.entity.AccWithdraw;
import com.eveb.saasops.modules.member.entity.MbrAuditAccount;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import tk.mybatis.mapper.common.IdsMapper;


@Mapper
public interface MbrAuditAccountMapper extends MyMapper<MbrAuditAccount>,IdsMapper<AccWithdraw> {

}
