package com.eveb.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class PaySet {

    @ApiModelProperty("是否启用自动出款 0禁用 1启用")
    private Integer payAutomatic;

    @ApiModelProperty("自动出款单笔最高限额(元)")
    private BigDecimal payMoney;

    @ApiModelProperty("是否启用免转 0禁用 1启用")
    private Integer freeWalletSwitch;
}
