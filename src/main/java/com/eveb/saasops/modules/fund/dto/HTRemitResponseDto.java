package com.eveb.saasops.modules.fund.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "RemitRequestDto", description = "汇通支付代付返回")
public class HTRemitResponseDto {

    @ApiModelProperty(value = "仅表示提交数据是否成功")
    private Boolean is_success;

    @ApiModelProperty(value = "签名数据")
    private String sign;

    @ApiModelProperty(value = "当失败时，返回的错误信息")
    private String errror_msg;

    @ApiModelProperty(value = "商户唯一订单号")
    private String transid;

    @ApiModelProperty(value = "支付平台订单号")
    private String order_id;

    @ApiModelProperty(value = "0 未处理 1 银行处理中 2 已打款 3失败")
    private String bank_status;

}
