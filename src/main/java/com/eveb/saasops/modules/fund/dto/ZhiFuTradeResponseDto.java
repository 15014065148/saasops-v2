package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "ZhiFuTradeRequestDto", description = "直付充值返回参数")
public class ZhiFuTradeResponseDto {

    @ApiModelProperty(value = "下单结果，成功：SUCCESS 失败：FAIL")
    private String status;

    @ApiModelProperty(value = "错误代码")
    private String code;

    @ApiModelProperty(value = "错误消息")
    private String message;

    @ApiModelProperty(value = "支付平台分配的应用ID")
    private String key;

    @ApiModelProperty(value = "支付平台流水号")
    private String sn;

    @ApiModelProperty(value = "商户订单号")
    private String trade_no;

    @ApiModelProperty(value = "订单金额，单位为分")
    private String money;

    @ApiModelProperty(value = "收银台页面地址")
    private String page_url;

    @ApiModelProperty(value = "提交的原始数据")
    private Object data;

    @ApiModelProperty(value = "返回数据的时间，格式：yyyyMMddHHmmss")
    private String time;

}
