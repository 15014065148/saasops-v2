package com.eveb.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@ApiModel(value = "JRescueRuleDto", description = "救援金活动规则")
public class RescueRuleDto {

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amountMin;

    @ApiModelProperty(value = "充值金额")
    private BigDecimal amountMax;

    @ApiModelProperty(value = "负盈利金额")
    private BigDecimal loseAmountMin;

    @ApiModelProperty(value = "负盈利金额")
    private BigDecimal loseAmountMax;

    @ApiModelProperty(value = "赠送类型 0按比例 1按金额")
    private Integer donateType;

    @ApiModelProperty(value = "赠送金额")
    private BigDecimal donateAmount;

    @ApiModelProperty(value = "赠送比例")
    private BigDecimal donateRatio;

    @ApiModelProperty(value = "赠送最高金额")
    private BigDecimal donateAmountMax;

    @ApiModelProperty(value = "优惠金额稽核流水倍数")
    private Double multipleWater;
}
