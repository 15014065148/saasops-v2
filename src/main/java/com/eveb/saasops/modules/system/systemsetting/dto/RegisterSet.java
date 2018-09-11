package com.eveb.saasops.modules.system.systemsetting.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
@Data
@Setter
@Getter
public class RegisterSet {
	@ApiModelProperty("会员账号")
	private Integer loginName;
	@ApiModelProperty("会员登录密码")
	private Integer loginPwd;
	@ApiModelProperty("会员重复密码")
	private Integer reLoginPwd;
	@ApiModelProperty("会员验证码")
	private Integer captchareg;
	@ApiModelProperty("会员真实姓名")
	private Integer realName;
	@ApiModelProperty("会员手机")
	private Integer mobile;
	@ApiModelProperty("会员邮箱")
	private Integer email;
	@ApiModelProperty("会员qq")
	private Integer qq;
	@ApiModelProperty("会员微信")
	private Integer weChat;
	@ApiModelProperty("会员地址")
	private Integer address;
	
	@ApiModelProperty("代理账号")
	private Integer agentLoginName;
	@ApiModelProperty("代理登录密码")
	private Integer agentLoginPwd;
	@ApiModelProperty("代理重复密码")
	private Integer agentReLoginPwd;
	@ApiModelProperty("代理验证码")
	private Integer agentCaptchareg;
	@ApiModelProperty("代理真实姓名")
	private Integer agentRealName;
	@ApiModelProperty("代理手机")
	private Integer agentMobile;
	@ApiModelProperty("代理邮箱")
	private Integer agentEmail;
	@ApiModelProperty("代理qq")
	private Integer agentQQ;
	@ApiModelProperty("代理微信")
	private Integer agentWechat;
	@ApiModelProperty("代理地址")
	private Integer agentAddress;
	

}
