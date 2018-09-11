package com.eveb.saasops.api.modules.user.dto;

import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@Setter
@ApiModel(value = "会员找回密码", description = "会员找回密码-选择找回密码方式!")
public class FindPwdDto {
	@ApiModelProperty(value = "会员账号,长度为6~10位!")
	private String userName;
	@ApiModelProperty(value = "验证码!")
	private String captcha;
}
