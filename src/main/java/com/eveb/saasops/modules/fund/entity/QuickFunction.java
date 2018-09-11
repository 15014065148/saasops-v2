package com.eveb.saasops.modules.fund.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "QuickFunction", description = "快捷功能")
public class QuickFunction {
    @ApiModelProperty(value = "统计条数")
    private Integer counts;

    @ApiModelProperty(value = "快捷功能名称")
    private String quickName;
}
