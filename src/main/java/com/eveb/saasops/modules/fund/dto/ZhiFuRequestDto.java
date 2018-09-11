package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "ZhiFuRequestDto", description = "直付充值回调参数")
public class ZhiFuRequestDto {

    @ApiModelProperty(value = "支付平台分配的应用ID")
    private String key;

    @ApiModelProperty(value = "支付平台流水号")
    private String sn;

    @ApiModelProperty(value = "商户订单号")
    private String trade_no;

    @ApiModelProperty(value = "商品名称")
    private String title;

    @ApiModelProperty(value = "订单备注")
    private String memo;

    @ApiModelProperty(value = "订单金额，单位为分")
    private String money;

    @ApiModelProperty(value = "支付宝交易号")
    private String sysid;

    @ApiModelProperty(value = "支付完成时间,UNIX时间戳")
    private String finish;

    @ApiModelProperty(value = "数据签名,详见签名算法")
    private String sign;

    @ApiModelProperty(value = "充值申请时间，格式：yyyyMMddHHmmss")
    private String time;

    @ApiModelProperty(value = "支付渠道，0为支付宝")
    private String platform;
}
