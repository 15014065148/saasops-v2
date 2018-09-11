package com.eveb.saasops.modules.log.entity;

import java.io.Serializable;


import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@ApiModel(value = "LogMbrregister", description = "")
@Table(name = "log_mbrregister")
public class LogMbrRegister implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "")
	private Integer accountId;

	@ApiModelProperty(value = "会员Id")
	private String loginName;

	@ApiModelProperty(value = "注册时间")
	private String registerTime;

	@ApiModelProperty(value = "注册网址")
	private String registerUrl;

	@ApiModelProperty(value = "注册IP")
	private String registerIp;

	@ApiModelProperty(value = "")
	private String regArea;

	@ApiModelProperty(value = "注册来源(0 PC,1后端管理台)")
	private Byte registerSource;


	public interface RegIpValue {
		Byte pcClient = 0;// 客户端
		Byte adminManage = 1;// 后端管理端
		Byte H5Client = 3;// H5端
		Byte adminClient = 2;//后端管理
	}
}