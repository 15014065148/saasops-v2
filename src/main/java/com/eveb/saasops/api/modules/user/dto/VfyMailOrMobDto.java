package com.eveb.saasops.api.modules.user.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户
 */
@Setter
@Getter
@ApiModel(value = "会员邮箱", description = "")
public class VfyMailOrMobDto implements Serializable {
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "会员邮箱!")
	private String email;
	@ApiModelProperty(value = "会员手机号!")
	private String mobile;
	@ApiModelProperty(value = "验证code!")
	private String code;
}