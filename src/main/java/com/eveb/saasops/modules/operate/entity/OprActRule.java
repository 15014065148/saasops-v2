package com.eveb.saasops.modules.operate.entity;

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
@ApiModel(value = "OprActRule", description = "OprActRule")
@Table(name = "opr_act_rule")
public class OprActRule implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "opr_act_activity ID")
    private Integer activityId;

    @ApiModelProperty(value = "规则字符串")
    private String rule;

    @ApiModelProperty(value = "time")
    private String time;
}