package com.eveb.saasops.modules.system.cmpydeposit.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;



@Setter
@Getter
@ApiModel(value = "SysDepMbr", description = "")
@Table(name = "set_basic_sys_dep_mbr")
public class SysDepMbr implements Serializable{
private static final long serialVersionUID=1L;


@ApiModelProperty(value = "")
private Integer memGroId;

@ApiModelProperty(value = "")
private Integer depositId;

}