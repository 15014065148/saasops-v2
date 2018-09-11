package com.eveb.saasops.api.modules.pay.pzpay.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Created by William on 2017/12/12.
 */
@Data
@ApiModel(value = "PzpayPayParams", description = "盘子支付支付入参")
public class PzpayPayParams {
	@ApiModelProperty(value = "充值数值，最小100,单位分")
	BigDecimal fee;
	@ApiModelProperty(value = "银行代码")
	String bankCode;
	@ApiModelProperty(value = "项目")
	String subject;
	@ApiModelProperty(value = "银行卡号")
	Long bankCardId;
	@ApiModelProperty(value = "备注")
	String extra;
	@ApiModelProperty(value = "支付类型 1盘子支付_支付宝 2盘子支付_微信 3盘子支付_信用卡 4盘子支付_连连借记卡 ,5线下支付")
	Integer payType;
	@ApiModelProperty(value = "ip")
	String ip;
	@JsonIgnore
	@ApiModelProperty(value = "会员Id")
	Integer accountId;
	@JsonIgnore
	@ApiModelProperty(value = "会员账号")
	String loginName;
	@JsonIgnore
	@ApiModelProperty(value = "会员姓名")
	String realName;
	@ApiModelProperty(value = "订单号")
	Long outTradeNo;
	@ApiModelProperty(value = "活动ID")
	Integer activityId;

	@ApiModelProperty(value = "0代表PC端,1代表手机")
	private Integer terminal;

	@ApiModelProperty(value = "存款ID",hidden = true)
	private Integer depositId;

	@ApiModelProperty(value = "客户端来源",hidden = true)
	private Byte fundSource;
}
