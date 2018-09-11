package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "RemitRequestDto", description = "汇通支付代付参数")
public class HTRemitRequestDto {

    @ApiModelProperty(value = "商户号")
    private String merchant_code;

    @ApiModelProperty(value = "签名")
    private String sign;

    @ApiModelProperty(value = "总金额单位元，保留2位小数")
    private String order_amount;

    @ApiModelProperty(value = "订单号")
    private String trade_no;

    @ApiModelProperty(value = "时间")
    private String order_time;

    @ApiModelProperty(value = "银行CODE")
    private String bank_code;

    @ApiModelProperty(value = "收款人姓名")
    private String account_name;

    @ApiModelProperty(value = "收款人卡号")
    private String account_number;
}
