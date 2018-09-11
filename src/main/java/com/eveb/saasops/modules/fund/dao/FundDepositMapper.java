package com.eveb.saasops.modules.fund.dao;

import com.eveb.saasops.modules.fund.entity.FundDeposit;
import org.apache.ibatis.annotations.Mapper;
import com.eveb.saasops.modules.base.mapper.MyMapper;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface FundDepositMapper extends MyMapper<FundDeposit> {

}
