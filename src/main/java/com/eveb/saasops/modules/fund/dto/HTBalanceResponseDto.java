package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "BalanceResponseDto", description = "查询商户余额返回")
public class HTBalanceResponseDto {

    @ApiModelProperty(value = "余额")
    private String money;

    @ApiModelProperty(value = "冻结金额")
    private String freeze_money;

    @ApiModelProperty(value = "状态")
    private Boolean is_success;

    @ApiModelProperty(value = "错误信息")
    private String errror_msg;

    @ApiModelProperty(value = "商户号")
    private String merchant_code;

    @ApiModelProperty(value = "签名")
    private String sign;
}
