package com.eveb.saasops.api.modules.user.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Table(name="mbr_retrvpw")
public class FindPwEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String loginName;//账号
	private String vaildCode;//验证码
	private Long expire;//失效时间
	private Integer vaildTimes;//验证次数
	private String applyTime;//申请时间
	private Byte vaildType;//验证方式
}
