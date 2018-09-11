package com.eveb.saasops.api.modules.transfer.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BbinPaginationDto {
    private Integer Page;
    private Integer PageLimit;
    private Integer TotalNumber;
    private Integer TotalPage;
}
