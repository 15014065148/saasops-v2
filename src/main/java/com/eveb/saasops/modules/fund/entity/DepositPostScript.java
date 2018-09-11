package com.eveb.saasops.modules.fund.entity;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(value = "公司入款申请成功后信息显示", description = "公司入款申请成功后信息显示")
@Getter
@Setter
public class DepositPostScript {
	@ApiModelProperty(value = "银行名称")
	private String bankName;
	@ApiModelProperty(value = "银行卡号 或第三方支付的账号")
	private String bankAccount;
	@ApiModelProperty(value = "真实姓名")
	private String realName;
	@ApiModelProperty(value = "支行名称")
	private String bankBranch;
	@ApiModelProperty(value = "申请存款金额")
	private BigDecimal depositAmount;
	@ApiModelProperty(value = "附言单号")
	private String depositPostscript;

}
