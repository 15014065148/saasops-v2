package com.eveb.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@ApiModel(value = "ActivityRequestDto", description = "活动DTO")
public class ActivityDto {

    @ApiModelProperty(value = "活动")
    private Object actActivity;

    @ApiModelProperty(value = "规则")
    private Object object;

}