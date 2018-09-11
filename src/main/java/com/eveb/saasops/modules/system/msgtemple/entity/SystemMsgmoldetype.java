package com.eveb.saasops.modules.system.msgtemple.entity;

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
@ApiModel(value = "SystemMsgmoldetype", description = "消息模板类型")
@Table(name = "set_basic_msgModelType")
public class SystemMsgmoldetype implements Serializable{
private static final long serialVersionUID=1L;
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@ApiModelProperty(value = "id")
private Integer id;

//名称
@ApiModelProperty(value = "名称")
private String name;
}