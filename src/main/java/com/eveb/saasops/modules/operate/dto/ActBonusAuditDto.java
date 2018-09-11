package com.eveb.saasops.modules.operate.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ActBonusAuditDto {

    @ApiModelProperty(value = "状态 0 拒绝 1通过")
    private Integer status;

    @ApiModelProperty(value = "ids")
    private List<Integer> bonuses;

    @ApiModelProperty(value = "活动分类编号")
    private String tmplCode;

    @ApiModelProperty(value = "备注")
    private String memo;

}
