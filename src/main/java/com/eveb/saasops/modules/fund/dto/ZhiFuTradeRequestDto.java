package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "ZhiFuTradeRequestDto", description = "直付充值参数")
public class ZhiFuTradeRequestDto {

    @ApiModelProperty(value = "支付平台分配的应用ID")
    private String key;

    @ApiModelProperty(value = "固定值：easypay.trade.pay")
    private String method;

    @ApiModelProperty(value = "商户订单号")
    private String trade_no;

    @ApiModelProperty(value = "商品名称")
    private String title;

    @ApiModelProperty(value = "商品名称")
    private String memo;

    @ApiModelProperty(value = "订单金额，单位为分")
    private String money;

    @ApiModelProperty(value = "支付渠道，暂时只支持支付宝，取值：alipay")
    private String platform;

    @ApiModelProperty(value = "移动端标记：移动端为Y ，PC端为N")
    private String mobile;

    @ApiModelProperty(value = "发送请求的时间，格式：yyyyMMddHHmmss")
    private String timestamp;

    @ApiModelProperty(value = "异步通知地址，用于接收支付结果回调")
    private String notify;

    @ApiModelProperty(value = "数据签名,详见签名算法1.1")
    private String sign;

    private String secret;
}
