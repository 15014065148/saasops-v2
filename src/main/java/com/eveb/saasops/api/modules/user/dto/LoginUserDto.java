package com.eveb.saasops.api.modules.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@ApiModel(value = "会员登陆", description = "当密码错误次数达到3次以上必须传送验证码!")
public class LoginUserDto {
	@ApiParam("会员账号")
	private String loginName;
	@ApiParam("会员密码")
	private String password;
	@ApiParam("验证码")
	private String captcha;
}
