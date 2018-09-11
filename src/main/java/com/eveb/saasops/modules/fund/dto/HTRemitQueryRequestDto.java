package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "RemitQueryRequestDto", description = "查询代付")
public class HTRemitQueryRequestDto {

    @ApiModelProperty(value = "商户号")
    private String merchant_code;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "当前时间")
    private String now_date;

    @ApiModelProperty(value = "商户平台订单号")
    private String trade_no;

    @ApiModelProperty(value = "支付平台订单号")
    private String order_no;
}
