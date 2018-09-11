package com.eveb.saasops.api.modules.apisys.entity;

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
@ApiModel(value = "站点url信息表", description = "站点url信息表")
@Table(name = "t_cp_siteurl")
public class TcpSiteurl implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@ApiModelProperty(value = "id")
	private Integer id;

	@ApiModelProperty(value = "站点Id")
	private Integer siteId;

	@ApiModelProperty(value = "站点代码")
	private String siteCode;

	@ApiModelProperty(value = "站点地址")
	private String siteUrl;
}