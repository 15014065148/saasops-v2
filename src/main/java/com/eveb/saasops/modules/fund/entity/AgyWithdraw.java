package com.eveb.saasops.modules.fund.entity;

import com.eveb.saasops.modules.base.entity.BaseAuth;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.persistence.*;


@Setter
@Getter
@ApiModel(value = "FundAgyWithdraw", description = "FundAgyWithdraw")
@Table(name = "fund_agy_withdraw")
public class AgyWithdraw implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "id")
    private Integer id;

    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "订单号")
    private Long orderNo;

    @ApiModelProperty(value = "订单前缀")
    private String orderPrefix;

    @ApiModelProperty(value = "代理ID（agy_account）")
    private Integer accountId;

    @ApiModelProperty(value = "提款状态(0 拒绝 1 通过 2待处理 3 出款中)")
    private Integer status;

    @ApiModelProperty(value = "提款金额")
    private BigDecimal drawingAmount;

    @ApiModelProperty(value = "转账手续费")
    private BigDecimal handlingCharge;

    @ApiModelProperty(value = "行政扣款")
    private BigDecimal cutAmount;

    @ApiModelProperty(value = "扣除优惠")
    private BigDecimal discountAmount;

    @ApiModelProperty(value = "实际出款")
    private BigDecimal actualArrival;

    @ApiModelProperty(value = "审核人")
    private String auditUser;

    @ApiModelProperty(value = "审核时间")
    private String auditTime;

    @ApiModelProperty(value = "出款人")
    private String passUser;

    @ApiModelProperty(value = "出款时间")
    private String passTime;

    @ApiModelProperty(value = "ip")
    private String ip;

    @ApiModelProperty(value = "memo")
    private String memo;

    @ApiModelProperty(value = "createUser")
    private String createUser;

    @ApiModelProperty(value = "createTime")
    private String createTime;

    @ApiModelProperty(value = "modifyUser")
    private String modifyUser;

    @ApiModelProperty(value = "modifyTime")
    private String modifyTime;

    @ApiModelProperty(value = "转账记录ID")
    private Integer billDetailId;

    @ApiModelProperty(value = "代理提款银行卡信息(bankcardId)")
    private Integer bankCardId;

    @Transient
    @ApiModelProperty(value = "代理提款次数")
    private Integer withdrawCount;

    @Transient
    @ApiModelProperty(value = "代理帐号")
    private String agyAccount;

    @Transient
    @ApiModelProperty(value = "是否首提 0 否 1 是")
    private Integer isWithdraw;

    @Transient
    @ApiModelProperty(value = "申请时间开始")
    private String createTimeFrom;

    @Transient
    @ApiModelProperty(value = "申请时间结束")
    private String createTimeTo;

    @Transient
    @ApiModelProperty(value = "提款信息")
    private AgyBankcard agyBankcard;

    @Transient
    @ApiModelProperty(value = "总代理")
    private String topAgyAccount;

    @Transient
    private BaseAuth baseAuth;
}