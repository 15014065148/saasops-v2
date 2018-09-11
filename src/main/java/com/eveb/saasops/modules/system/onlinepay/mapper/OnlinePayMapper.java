package com.eveb.saasops.modules.system.onlinepay.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.eveb.saasops.api.modules.pay.pzpay.dto.OnlinePayDto;

@Mapper
public interface OnlinePayMapper {

	OnlinePayDto findPaymentInfo(@Param("id") Integer id);
}
