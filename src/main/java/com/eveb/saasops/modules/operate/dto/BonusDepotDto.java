package com.eveb.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class BonusDepotDto {

    @ApiModelProperty(value = "平台id")
    private Integer depotId;

    @ApiModelProperty(value = "平台名称")
    private String depotName;

}
