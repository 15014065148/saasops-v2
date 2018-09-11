package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "ZhiFuResponseDto", description = "直付充值回调返回参数")
public class ZhiFuResponseDto {

    @ApiModelProperty(value = "充值结果响应，成功：SUCCESS 失败：FAIL")
    private String status;

    @ApiModelProperty(value = "错误消息")
    private String message;

    @ApiModelProperty(value = "支付平台分配的应用ID")
    private String key;

    @ApiModelProperty(value = "支付平台流水号")
    private String sn;

    @ApiModelProperty(value = "商户订单号")
    private String trade_no;

    @ApiModelProperty(value = "数据签名,详见签名算法")
    private String sign;

}
