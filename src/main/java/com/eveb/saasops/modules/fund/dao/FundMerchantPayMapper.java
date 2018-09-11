package com.eveb.saasops.modules.fund.dao;

import com.eveb.saasops.modules.base.mapper.MyMapper;
import com.eveb.saasops.modules.fund.entity.FundMerchantPay;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;


@Component
@Mapper
public interface FundMerchantPayMapper extends MyMapper<FundMerchantPay> {

}
