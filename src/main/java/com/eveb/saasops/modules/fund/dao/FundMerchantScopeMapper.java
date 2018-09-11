package com.eveb.saasops.modules.fund.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.fund.entity.FundMerchantScope;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.IdsMapper;


@Component
@Mapper
public interface FundMerchantScopeMapper extends MyMapper<FundMerchantScope>, IdsMapper<FundMerchantScope> {

}
