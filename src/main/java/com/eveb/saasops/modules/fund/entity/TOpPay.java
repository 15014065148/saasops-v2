package com.eveb.saasops.modules.fund.entity;

import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.Transient;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;


@Setter
@Getter
@ApiModel(value = "TOpPay", description = "支付渠道")
@Table(name = "t_op_pay")
public class TOpPay implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @ApiModelProperty(value = "支付机构")
    private String payment;

    @ApiModelProperty(value = "")
    private String payClass;

    @ApiModelProperty(value = "")
    private String payWay;

    @ApiModelProperty(value = "支付方式 1：pc ，2：移动，3：pc+移动")
    private Byte payType;

    @ApiModelProperty(value = "是否启用 1启用 0 不启用")
    private Byte isEnable;

    @ApiModelProperty(value = "创建人")
    private String createUser;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "修改人")
    private String modifyUser;

    @ApiModelProperty(value = "最后一次修改时间")
    private String modifyTime;

    @ApiModelProperty(value = "移动端渠道支付LOGO")
    private String mBankLog;

    @ApiModelProperty(value = "支付域名")
    private String tPayUrl;

    @ApiModelProperty(value = "支付回调域名")
    private String callbackUrl;
    
    @Transient
    @ApiModelProperty(value = "会员ID")
    private String accountId;
    
    @Transient
    @ApiModelProperty(value = "区分移动端与PC端")
    private String terminal;

    @Transient
    @ApiModelProperty(value = "区分设备三端;PC:0 H5:3")
    private Byte devSource;
}