package com.eveb.saasops.modules.member.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDto{

    @ApiModelProperty(value = "会员名")
    private String loginName;

    @ApiModelProperty(value = "")
    private String item;

}
