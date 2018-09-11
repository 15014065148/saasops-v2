package com.eveb.saasops.api.modules.transfer.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BbinResponseDto {
	@ApiModelProperty(value="回傳結果(true or false)")
    private Boolean result;
    private BbinDataDto data;
}
