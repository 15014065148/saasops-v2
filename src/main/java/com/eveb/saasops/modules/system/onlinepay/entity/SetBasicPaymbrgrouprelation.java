package com.eveb.saasops.modules.system.onlinepay.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;



@Setter
@Getter
@ApiModel(value = "SetBasicPaymbrgrouprelation", description = "")
@Table(name = "set_basic_PayMbrGroupRelation")
public class SetBasicPaymbrgrouprelation implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

//
@ApiModelProperty(value = "")
private Integer mbrGroupId;
//
@ApiModelProperty(value = "")
private Integer onlinePayId;
}