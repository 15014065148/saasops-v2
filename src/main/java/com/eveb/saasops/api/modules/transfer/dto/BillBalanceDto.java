package com.eveb.saasops.api.modules.transfer.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class BillBalanceDto {
    @ApiModelProperty(value = "游戏平台名字")
    private String depotName;
    @ApiModelProperty(value = "平台操作前余额")
    private BigDecimal depotBeforeBalance;
    @ApiModelProperty(value = "是否成功 true是  false否")
    private Boolean isTransferOut = Boolean.FALSE;
}
