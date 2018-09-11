package com.eveb.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepotFailDto {
    @ApiModelProperty(value = "平台")
    private Integer depotId;

    @ApiModelProperty(value = "false 证明有失败的 true成功")
    private Boolean failError;

    @ApiModelProperty(value = "0线路 1不满足稽核")
    private Integer isSign;
}
