package com.eveb.saasops.api.modules.user.dto;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import com.eveb.saasops.common.validator.group.AddGroup;
import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;



/**
 * 用户
 */
@Setter
@Getter
@ApiModel(value = "会员注册参数,参数必传与不传请参照接口", description = "")
public class UserDto implements Serializable {
	private static final long serialVersionUID = 1L;
	@ApiModelProperty(value = "会员账号,长度为6~10位!")
	private String loginName;
	
	@ApiModelProperty(value = "会员密码,长度为6~18位!")
	private String loginPwd;
	
    @ApiModelProperty(value="验证码")
	private String captchareg;
    
    @ApiModelProperty(value="代理推广代码长度最大为6位,可选.")
    private String spreadCode;
    
    @ApiModelProperty(value="代理推广代码长度最大为16位,可选.")
    private String realName;
    
    @ApiModelProperty(value="手机最大长度最大为11位,可选.")
    private String mobile;
    
    @ApiModelProperty(value="邮箱最长为30位,可选.")
    private String email;
    
    @ApiModelProperty(value="手机最大长度最大为11位,可选.")
    private String qq;
    
    @ApiModelProperty(value="微信最大长度最大为20位,可选.")
    private String weChat;
    
    @ApiModelProperty(value="地址最大长度最大为50位,可选.")
    private String address;
}
