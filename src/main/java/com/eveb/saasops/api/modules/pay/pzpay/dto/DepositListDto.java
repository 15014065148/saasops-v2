package com.eveb.saasops.api.modules.pay.pzpay.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepositListDto {
	@ApiModelProperty(value = "0 线上入款 ,1 公司入款，2 人工充值 虚拟到其它的表 3全部")
	private Integer mark;
	@ApiModelProperty(value = "开始时间 yyyy-MM-dd HH:mm:ss")
	private String createTimeFrom;
	@ApiModelProperty(value = "结束时间 yyyy-MM-dd HH:mm:ss")
	private String createTimeTo;
	@ApiModelProperty(value = "会员ID(mbr_account）")
	@JsonIgnore
	private Integer accountId;
}
