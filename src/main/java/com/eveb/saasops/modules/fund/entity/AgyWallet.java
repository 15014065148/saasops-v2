package com.eveb.saasops.modules.fund.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "AgyWallet", description = "AgyWallet")
@Table(name = "agy_wallet")
public class AgyWallet implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理ID")
    private Integer accountId;

    @ApiModelProperty(value = "代理账号")
    private String loginName;

    @ApiModelProperty(value = "会员资金余额")
    private BigDecimal balance;

    @ApiModelProperty(value = "总余额")
    private BigDecimal totalBalance;

    @ApiModelProperty(value = "总存款")
    private BigDecimal totalDeposit;

    @ApiModelProperty(value = "总取款")
    private BigDecimal totalWithdrawal;

}