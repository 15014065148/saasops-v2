package com.eveb.saasops.api.modules.transfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class BbinUsrBalanceDto {
    private String LoginName;
    private String Currency;
    private BigDecimal Balance;
    private BigDecimal TotalBalance;
}
