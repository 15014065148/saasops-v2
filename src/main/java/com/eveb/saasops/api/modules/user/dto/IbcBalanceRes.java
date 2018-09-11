package com.eveb.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class IbcBalanceRes {
	private String playerName;
	private BigDecimal balance;
	private BigDecimal outstanding;
	private Integer currency;
}
