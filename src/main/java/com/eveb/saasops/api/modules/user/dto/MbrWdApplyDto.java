package com.eveb.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "会员取款申请JSON", description = "会员取款申请JSON")
public class MbrWdApplyDto {
	
	@ApiModelProperty(value = "会员提款银行卡信息(bankcardId)")
	private Integer bankCardId;
	
	@ApiModelProperty(value = "提款金额")
	private BigDecimal drawingAmount;
	
	@ApiModelProperty(value = "会员资金密码，必填")
	private String pwd;
}
