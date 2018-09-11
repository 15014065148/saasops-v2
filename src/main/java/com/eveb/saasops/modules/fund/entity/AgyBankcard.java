package com.eveb.saasops.modules.fund.entity;

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
@ApiModel(value = "AgyBankcard", description = "AgyBankcard")
@Table(name = "agy_bankcard")
public class AgyBankcard implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "代理Id")
    private Integer accountId;

    @ApiModelProperty(value = "银行名称Id")
    private String bankName;

    @ApiModelProperty(value = "银行卡号")
    private String cardNo;

    @ApiModelProperty(value = "")
    private String province;

    @ApiModelProperty(value = "")
    private String city;

    @ApiModelProperty(value = "")
    private String address;

    @ApiModelProperty(value = "")
    private String realName;

    @ApiModelProperty(value = "1开启, 0禁用")
    private Byte available;

    @ApiModelProperty(value = "")
    private String createTime;

    @ApiModelProperty(value = "1删除，0未删除")
    private Byte isDel;

}