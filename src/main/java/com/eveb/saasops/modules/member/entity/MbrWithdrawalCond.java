package com.eveb.saasops.modules.member.entity;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "MbrWithdrawalCond", description = "")
@Table(name = "mbr_withdrawal_cond")
public class MbrWithdrawalCond implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	//@JsonIgnore
	private Integer id;

	//@JsonIgnore
	@ApiModelProperty(value = "会员组ID")
	private Integer groupId;

	@ApiModelProperty(value = "最低限额CNY")
	private BigDecimal lowQuota;

	@ApiModelProperty(value = "最高限额CNY")
	private BigDecimal topQuota;

	//@JsonIgnore
	@ApiModelProperty(value = "手续费-时限/小时")
	private Integer feeHours;

	//@JsonIgnore
	@ApiModelProperty(value = "手续费-限免次数")
	private Integer feeTimes;

	//@JsonIgnore
	@ApiModelProperty(value = "手续上限金额CNY")
	private BigDecimal feeTop;

	//@JsonIgnore
	@ApiModelProperty(value = "手续费 比例")
	private BigDecimal feeScale;

	//@JsonIgnore
	@ApiModelProperty(value = "手续费固定收费费用")
	private BigDecimal feeFixed;

	//@JsonIgnore
	@ApiModelProperty(value = "收取手续费方式(按比例、固定收费)")
	private Byte feeWay;

	
	@ApiModelProperty(value = "取款限制")
	private Byte feeAvailable;

	
	@ApiModelProperty(value = "每日充许取款次数")
	private Integer withDrawalTimes;

	@ApiModelProperty(value = "每日取款限额")
	private BigDecimal withDrawalQuota;

	//@JsonIgnore
	@ApiModelProperty(value = "存款稽核")
	private Integer withDrawalAudit;

	//@JsonIgnore
	@ApiModelProperty(value = "管理费、行政费")
	private Integer manageFee;

	//@JsonIgnore
	@ApiModelProperty(value = "放宽额度CNY ")
	private BigDecimal overFee;
	
	public interface FeeWayVal
	{
		byte scale=0;//比率
		byte fixed=1;//固定
	}
}