package com.eveb.saasops.api.modules.transfer.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class BbinUsrBalanceResponseDto {
    private Boolean result;
    private BbinPaginationDto pagination;
    private ArrayList<BbinUsrBalanceDto> data;
}
