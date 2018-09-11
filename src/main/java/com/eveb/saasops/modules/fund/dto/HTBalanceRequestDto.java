package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "BalanceRequestDto", description = "查询商户余额")
public class HTBalanceRequestDto {

    @ApiModelProperty(value = "商户号")
    private String merchant_code;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "当前时间")
    private String query_time;
}
