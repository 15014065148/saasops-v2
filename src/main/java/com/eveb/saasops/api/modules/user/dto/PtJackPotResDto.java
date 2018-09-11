package com.eveb.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Setter;
import lombok.Getter;
@Getter
@Setter
@ApiModel(value="PT 奖池返回金额")
public class PtJackPotResDto {
	@ApiModelProperty(value="其它信息")
	 private String info;//其它信息
	 private String amounts;//金额
}
