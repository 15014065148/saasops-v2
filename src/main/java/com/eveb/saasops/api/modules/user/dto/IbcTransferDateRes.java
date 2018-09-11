package com.eveb.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class IbcTransferDateRes {
	private String trans_id;
	private BigDecimal before_amount;
	private BigDecimal after_amount;
	private Integer status;
	private BigDecimal amount;
	private String transfer_date;
	private Integer currency;
}
