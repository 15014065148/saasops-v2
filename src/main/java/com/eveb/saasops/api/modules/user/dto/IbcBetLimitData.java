package com.eveb.saasops.api.modules.user.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class IbcBetLimitData {
	private String sport_type;
	private BigDecimal min_bet;
	private BigDecimal max_bet;
	private BigDecimal max_bet_per_match;
	private BigDecimal max_bet_per_ball;
}
