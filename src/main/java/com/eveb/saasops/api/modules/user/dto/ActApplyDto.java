package com.eveb.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "活动申请DTO",description = "活动申请DTO")
public class ActApplyDto {
	@ApiModelProperty(value="活动ID号")
	private int activityId;
}
